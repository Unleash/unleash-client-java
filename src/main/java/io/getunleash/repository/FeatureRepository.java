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
    private AtomicInteger skips = new AtomicInteger(0);
    private final Integer maxSkips;

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
        this.maxSkips = this.calculateMaxSkips((int) unleashConfig.getFetchTogglesInterval());
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
        this.maxSkips = this.calculateMaxSkips((int) unleashConfig.getFetchTogglesInterval());
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
        this.maxSkips = this.calculateMaxSkips((int) unleashConfig.getFetchTogglesInterval());
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

    private Integer calculateMaxSkips(int fetchTogglesInterval) {
        return Integer.max(20, 300 / Integer.max(fetchTogglesInterval, 1));
    }

    private Runnable updateFeatures(@Nullable final Consumer<UnleashException> handler) {
        return () -> updateFeaturesInternal(handler);
    }

    private void updateFeaturesInternal(@Nullable final Consumer<UnleashException> handler) {
        if (skips.get() <= 0L) {
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
                decrementFailureCountAndResetSkips();
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
            skips.decrementAndGet(); // We didn't do anything this iteration, just reduce the count
        }
    }

    /**
     * We've had one successful call, so if we had 10 failures in a row, this will reduce the skips
     * down to 9, so that we gradually start polling more often, instead of doing max load
     * immediately after a sequence of errors.
     */
    private void decrementFailureCountAndResetSkips() {
        skips.set(Math.max(failures.decrementAndGet(), 0));
    }

    /**
     * We've gotten the message to back off (usually a 429 or a 50x). If we have successive
     * failures, failure count here will be incremented higher and higher which will handle
     * increasing our backoff, since we set the skip count to the failure count after every reset
     */
    private void increaseSkipCount() {
        skips.set(Math.min(failures.incrementAndGet(), maxSkips));
    }

    /**
     * We've received an error code that we don't expect to change, which means we've already logged
     * an ERROR. To avoid hammering the server that just told us we did something wrong and to avoid
     * flooding the logs, we'll increase our skip count to maximum
     */
    private void maximizeSkips() {
        skips.set(maxSkips);
        failures.incrementAndGet();
    }

    private void handleHttpErrorCodes(int responseCode) {
        if (responseCode == 404) {
            maximizeSkips();
            LOGGER.error(
                    "Server said that the API at {} does not exist. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashAPI(),
                    maxSkips);
        } else if (responseCode == 429) {
            increaseSkipCount();
            LOGGER.info(
                    "Client was RATE LIMITED for the {}. time. Further backing off. Current backoff at {} times our poll interval",
                    failures.get(),
                    skips.get());
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            maximizeSkips();
            LOGGER.error(
                    "Client failed to authenticate to the Unleash API at {}. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashAPI(),
                    maxSkips);
        } else if (responseCode >= 500) {
            increaseSkipCount();
            LOGGER.info(
                    "Server failed with a {} status code. Backing off. Current backoff at {} times our poll interval",
                    responseCode,
                    skips.get());
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

    public Integer getSkips() {
        return skips.get();
    }
}
