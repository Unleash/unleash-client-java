package no.finn.unleash.repository;

import java.util.List;
import java.util.stream.Collectors;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.UnleashException;
import no.finn.unleash.event.EventDispatcher;
import no.finn.unleash.event.UnleashReady;
import no.finn.unleash.lang.Nullable;
import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;

public final class FeatureToggleRepository implements ToggleRepository {
    private final ToggleBootstrapHandler toggleBootstrapHandler;
    private final ToggleBackupHandler toggleBackupHandler;
    private final ToggleFetcher toggleFetcher;
    private final EventDispatcher eventDispatcher;

    private ToggleCollection toggleCollection;
    private boolean ready;

    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            ToggleFetcher toggleFetcher,
            ToggleBackupHandler toggleBackupHandler) {
        this(
                unleashConfig,
                unleashConfig.getScheduledExecutor(),
                toggleFetcher,
                toggleBackupHandler,
                new ToggleBootstrapHandlerFile(unleashConfig));
    }

    @Deprecated
    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            UnleashScheduledExecutor executor,
            ToggleFetcher toggleFetcher,
            ToggleBackupHandler toggleBackupHandler) {
        this(
                unleashConfig,
                executor,
                toggleFetcher,
                toggleBackupHandler,
                new ToggleBootstrapHandlerFile(unleashConfig));
    }

    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            UnleashScheduledExecutor executor,
            ToggleFetcher toggleFetcher,
            ToggleBackupHandler toggleBackupHandler,
            ToggleBootstrapHandler toggleBootstrapHandler) {

        this.toggleBootstrapHandler = toggleBootstrapHandler;
        this.toggleBackupHandler = toggleBackupHandler;
        this.toggleFetcher = toggleFetcher;
        this.eventDispatcher = new EventDispatcher(unleashConfig);

        toggleCollection = toggleBackupHandler.read();
        if (toggleCollection.getFeatures().isEmpty()) {
            toggleCollection = toggleBootstrapHandler.readAndValidate();
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
                .map(toggle -> toggle.getName())
                .collect(Collectors.toList());
    }
}
