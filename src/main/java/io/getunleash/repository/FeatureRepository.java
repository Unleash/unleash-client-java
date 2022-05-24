package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashReady;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FeatureRepository implements IFeatureRepository {
    private final FeatureBackupHandlerFile featureBackupHandler;
    private final FeatureBootstrapHandler featureBootstrapHandler;
    private final HttpFeatureFetcher featureFetcher;
    private final EventDispatcher eventDispatcher;

    private FeatureCollection featureCollection;
    private boolean ready;

    private static FeatureRepository instance = null;

    private FeatureRepository(UnleashConfig unleashConfig) {
        this.featureBackupHandler = FeatureBackupHandlerFile.init(unleashConfig);
        this.featureFetcher = HttpFeatureFetcher.init(unleashConfig);
        this.featureBootstrapHandler = FeatureBootstrapHandler.init(unleashConfig);

        this.eventDispatcher = new EventDispatcher(unleashConfig);
        this.featureCollection = featureBackupHandler.read();
        if (this.featureCollection.getToggleCollection().getFeatures().isEmpty()) {
            this.featureCollection = featureBootstrapHandler.read();
        }

        if (unleashConfig.isSynchronousFetchOnInitialisation()) {
            updateFeatures().run();
        }

        unleashConfig
                .getScheduledExecutor()
                .setInterval(updateFeatures(), 0, unleashConfig.getFetchTogglesInterval());
    }

    public static synchronized FeatureRepository getInstance() {
        if (instance == null) {
            throw new AssertionError("FeatureRepository:: You have to call init first");
        }
        return instance;
    }

    public static synchronized FeatureRepository init(UnleashConfig unleashConfig) {
        if (instance != null) {
            return instance;
        }
        instance = new FeatureRepository(unleashConfig);
        return instance;
    }

    private Runnable updateFeatures() {
        return () -> {
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
