package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashReady;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.List;
import java.util.stream.Collectors;
@Deprecated()
public final class FeatureToggleRepository implements ToggleRepository {
    private final BackupHandler<ToggleCollection> toggleBackupHandler;
    private final ToggleBootstrapHandler toggleBootstrapHandler;
    private final ToggleFetcher toggleFetcher;
    private final EventDispatcher eventDispatcher;

    private ToggleCollection toggleCollection;
    private boolean ready;

    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            ToggleFetcher toggleFetcher,
            BackupHandler<ToggleCollection> toggleBackupHandler) {
        this(
                unleashConfig,
                unleashConfig.getScheduledExecutor(),
                toggleFetcher,
                toggleBackupHandler);
    }

    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            UnleashScheduledExecutor executor,
            ToggleFetcher toggleFetcher,
            BackupHandler<ToggleCollection> toggleBackupHandler) {

        this.toggleBackupHandler = toggleBackupHandler;
        this.toggleFetcher = toggleFetcher;
        this.eventDispatcher = new EventDispatcher(unleashConfig);
        this.toggleBootstrapHandler = new ToggleBootstrapHandler(unleashConfig);
        this.toggleCollection = toggleBackupHandler.read();
        if (this.toggleCollection.getFeatures().isEmpty()) {
            this.toggleCollection = toggleBootstrapHandler.read();
        }

        if (unleashConfig.isSynchronousFetchOnInitialisation()) {
            updateToggles().run();
        }

        executor.setInterval(updateToggles(), 0, unleashConfig.getFetchTogglesInterval());
    }

    private Runnable updateToggles() {
        return () -> {
            try {
                FeatureToggleResponse response = toggleFetcher.fetchToggles();
                eventDispatcher.dispatch(response);
                if (response.getStatus() == FeatureToggleResponse.Status.CHANGED) {
                    toggleCollection = response.getToggleCollection();
                    toggleBackupHandler.write(response.getToggleCollection());
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
        return toggleCollection.getToggle(name);
    }

    @Override
    public List<String> getFeatureNames() {
        return toggleCollection.getFeatures().stream()
                .map(FeatureToggle::getName)
                .collect(Collectors.toList());
    }
}
