package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashReady;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureRepository implements IFeatureRepository {
    private final BackupHandler<FeatureCollection> featureBackupHandler;
    private final FeatureBootstrapHandler featureBootstrapHandler;
    private final FeatureFetcher featureFetcher;
    private final EventDispatcher eventDispatcher;

    private FeatureCollection featureCollection;
    private boolean ready;

    public FeatureRepository(
            UnleashConfig unleashConfig,
            FeatureFetcher fetcher,
            BackupHandler<FeatureCollection> backupHandler) {
        this(unleashConfig, unleashConfig.getScheduledExecutor(), fetcher, backupHandler);
    }

    public FeatureRepository(
            UnleashConfig unleashConfig,
            UnleashScheduledExecutor executor,
            FeatureFetcher featureFetcher,
            BackupHandler<FeatureCollection> featureBackupHandler) {

        this.featureBackupHandler = featureBackupHandler;
        this.featureFetcher = featureFetcher;
        this.eventDispatcher = new EventDispatcher(unleashConfig);
        this.featureBootstrapHandler = new FeatureBootstrapHandler(unleashConfig);
        this.featureCollection = featureBackupHandler.read();
        if (this.featureCollection.getToggleCollection().getFeatures().isEmpty()) {
            this.featureCollection = featureBootstrapHandler.read();
        }

        if (unleashConfig.isSynchronousFetchOnInitialisation()) {
            updateFeatures().run();
        }

        executor.setInterval(updateFeatures(), 0, unleashConfig.getFetchTogglesInterval());
    }

    private Runnable updateFeatures() {
        return () -> {
            try {
                ClientFeaturesResponse response = featureFetcher.fetchFeatures();
                eventDispatcher.dispatch(response);
                if (response.getStatus() == ClientFeaturesResponse.Status.CHANGED) {
                    featureCollection =
                            new FeatureCollection(
                                    response.getToggleCollection(),
                                    response.getSegmentCollection());
                    featureBackupHandler.write(featureCollection);
                }

                if (!ready) {
                    eventDispatcher.dispatch(new UnleashReady());
                    ready = true;
                }
            } catch (UnleashException e) {
                eventDispatcher.dispatch(e);
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
}
