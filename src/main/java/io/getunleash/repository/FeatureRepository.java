package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashReady;
import io.getunleash.lang.Nullable;
import io.getunleash.util.Throttler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.Collections;
import java.util.List;
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

    private final Throttler throttler;

    private FeatureCollection featureCollection;
    private boolean ready;

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
        this.throttler =
                new Throttler(
                        (int) unleashConfig.getFetchTogglesInterval(),
                        300,
                        unleashConfig.getUnleashURLs().getFetchTogglesURL());
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
        this.throttler =
                new Throttler(
                        (int) unleashConfig.getFetchTogglesInterval(),
                        300,
                        unleashConfig.getUnleashURLs().getFetchTogglesURL());
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
        this.throttler =
                new Throttler(
                        (int) unleashConfig.getFetchTogglesInterval(),
                        300,
                        unleashConfig.getUnleashURLs().getFetchTogglesURL());
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
        return () -> {
            if (throttler.performAction()) {
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
                        throttler.handleHttpErrorCodes(response.getHttpStatusCode());
                        return;
                    }
                    throttler.decrementFailureCountAndResetSkips();
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
                throttler.skipped(); // We didn't do anything this iteration, just reduce the count
            }
        };
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
        return this.throttler.getFailures();
    }

    public Integer getSkips() {
        return this.throttler.getSkips();
    }
}
