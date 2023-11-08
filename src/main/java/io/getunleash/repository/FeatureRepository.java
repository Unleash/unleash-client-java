package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashReady;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureRepository implements IFeatureRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureRepository.class);
    private final UnleashConfig unleashConfig;
    private final BackupHandler<FeatureCollection> featureBackupHandler;
    private final FeatureBootstrapHandler featureBootstrapHandler;
    private final FeatureFetcher featureFetcher;
    private final EventDispatcher eventDispatcher;

    private FeatureCollection featureCollection;
    private boolean ready;

    private AtomicInteger failures = new AtomicInteger(0);
    private AtomicInteger interval = new AtomicInteger(0);
    private final Integer maxInterval;

    public FeatureRepository(UnleashConfig unleashConfig) {
        this(unleashConfig, new FeatureBackupHandlerFile(unleashConfig));
    }

    public FeatureRepository(
            UnleashConfig unleashConfig,
            final BackupHandler<FeatureCollection> featureBackupHandler) {
        this.unleashConfig = unleashConfig;
        this.featureBackupHandler = featureBackupHandler;
        this.featureFetcher = unleashConfig.getUnleashFeatureFetcherFactory().apply(unleashConfig);
        this.featureBootstrapHandler = new FeatureBootstrapHandler(unleashConfig);
        this.eventDispatcher = new EventDispatcher(unleashConfig);
        this.maxInterval =
                Integer.max(
                        20,
                        300
                                / Integer.max(
                                        Long.valueOf(unleashConfig.getFetchTogglesInterval())
                                                .intValue(),
                                        1));

        this.initCollections(unleashConfig.getScheduledExecutor());
    }

    protected FeatureRepository(
            UnleashConfig unleashConfig,
            BackupHandler<FeatureCollection> featureBackupHandler,
            EventDispatcher eventDispatcher,
            FeatureFetcher featureFetcher,
            FeatureBootstrapHandler featureBootstrapHandler) {

        this.unleashConfig = unleashConfig;
        this.featureBackupHandler = featureBackupHandler;
        this.featureFetcher = featureFetcher;
        this.featureBootstrapHandler = featureBootstrapHandler;
        this.eventDispatcher = eventDispatcher;
        this.maxInterval =
                Integer.max(
                        20,
                        300
                                / Integer.max(
                                        Long.valueOf(unleashConfig.getFetchTogglesInterval())
                                                .intValue(),
                                        1));
        this.initCollections(unleashConfig.getScheduledExecutor());
    }

    protected FeatureRepository(
            UnleashConfig unleashConfig,
            FeatureBackupHandlerFile featureBackupHandler,
            UnleashScheduledExecutor executor,
            FeatureFetcher featureFetcher,
            FeatureBootstrapHandler featureBootstrapHandler) {
        this.unleashConfig = unleashConfig;
        this.featureBackupHandler = featureBackupHandler;
        this.featureFetcher = featureFetcher;
        this.featureBootstrapHandler = featureBootstrapHandler;
        this.eventDispatcher = new EventDispatcher(unleashConfig);
        this.maxInterval =
                Integer.max(
                        20,
                        300
                                / Integer.max(
                                        Long.valueOf(unleashConfig.getFetchTogglesInterval())
                                                .intValue(),
                                        1));
        this.initCollections(executor);
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void initCollections(UnleashScheduledExecutor executor) {
        this.featureCollection = this.featureBackupHandler.read();
        if (this.featureCollection.getToggleCollection().getFeatures().isEmpty()) {
            this.featureCollection = this.featureBootstrapHandler.read();
        }

        if (unleashConfig.isSynchronousFetchOnInitialisation()) {
            updateFeatures(null).run();
        }

        if (!unleashConfig.isDisablePolling()) {
            Runnable updateFeatures = updateFeatures(this.eventDispatcher::dispatch);
            if (unleashConfig.getFetchTogglesInterval() > 0) {
                executor.setInterval(updateFeatures, 0, unleashConfig.getFetchTogglesInterval());
            } else {
                executor.scheduleOnce(updateFeatures);
            }
        }
    }

    private Runnable updateFeatures(@Nullable final Consumer<UnleashException> handler) {
        return () -> updateFeaturesInternal(handler);
    }

    private void updateFeaturesInternal(@Nullable final Consumer<UnleashException> handler) {
        if (interval.get() <= 0L) {
            try {
                ClientFeaturesResponse response = featureFetcher.fetchFeatures();
                eventDispatcher.dispatch(response);
                if (response.getStatus() == ClientFeaturesResponse.Status.CHANGED) {
                    SegmentCollection segmentCollection = response.getSegmentCollection();
                    featureCollection =
                            new FeatureCollection(
                                    response.getToggleCollection(),
                                    segmentCollection != null
                                            ? segmentCollection
                                            : new SegmentCollection(Collections.emptyList()));

                    featureBackupHandler.write(featureCollection);
                } else if (response.getStatus() == ClientFeaturesResponse.Status.UNAVAILABLE) {
                    handleHttpErrorCodes(response.getHttpStatusCode());
                    return;
                }
                interval.set(Math.max(failures.decrementAndGet(), 0));
                if (!ready) {
                    eventDispatcher.dispatch(new UnleashReady());
                    ready = true;
                }
            } catch (UnleashException e) {
                if (handler != null) {
                    handler.accept(e);
                } else {
                    throw e;
                }
            }
        } else {
            interval.decrementAndGet();
        }
    }

    private void handleHttpErrorCodes(int responseCode) {
        if (responseCode == 404) {
            interval.set(maxInterval);
            failures.incrementAndGet();
            LOGGER.error(
                    "Server said that the API at {} does not exist. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashAPI(),
                    maxInterval);
        } else if (responseCode == 429) {
            interval.set(Math.min(failures.incrementAndGet(), maxInterval));
            LOGGER.info(
                    "Client was RATE LIMITED for the {} time. Further backing off. Current backoff at {} times our poll interval",
                    failures.get(),
                    interval.get());
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            failures.incrementAndGet();
            interval.set(maxInterval);
            LOGGER.error(
                    "Client failed to authenticate to the Unleash API at {}. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashAPI(),
                    maxInterval);
        } else if (responseCode >= 500) {
            interval.set(Math.min(failures.incrementAndGet(), maxInterval));
            LOGGER.info(
                    "Server failed with a {} status code. Backing off. Current backoff at {} times our poll interval",
                    responseCode,
                    interval.get());
        }
    }

    @Override
    public @Nullable FeatureToggle getToggle(String name) {
        return featureCollection.getToggleCollection().getToggle(name);
    }

    @Override
    public List<String> getFeatureNames() {
        return featureCollection.getToggleCollection().getFeatures().stream()
                .map(FeatureToggle::getName)
                .collect(Collectors.toList());
    }

    @Override
    public Segment getSegment(Integer id) {
        return featureCollection.getSegmentCollection().getSegment(id);
    }

    public Integer getFailures() {
        return failures.get();
    }

    public Integer getInterval() {
        return interval.get();
    }
}
