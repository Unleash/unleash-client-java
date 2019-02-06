package no.finn.unleash.repository;

import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

import no.finn.unleash.FeatureToggle;
import no.finn.unleash.UnleashException;

public final class FeatureToggleRepository implements ToggleRepository {
    private static final Logger LOG = LogManager.getLogger(FeatureToggleRepository.class);

    private final ToggleBackupHandler toggleBackupHandler;
    private final ToggleFetcher toggleFetcher;

    private ToggleCollection toggleCollection;

    public FeatureToggleRepository(
            UnleashConfig unleashConfig,
            UnleashScheduledExecutor executor,
            ToggleFetcher toggleFetcher,
            ToggleBackupHandler toggleBackupHandler) {

        this.toggleBackupHandler = toggleBackupHandler;
        this.toggleFetcher = toggleFetcher;

        toggleCollection = toggleBackupHandler.read();

        if(unleashConfig.isSynchronousFetchOnInitialisation()){
            updateToggles().run();
        }

        executor.setInterval(updateToggles(), 0, unleashConfig.getFetchTogglesInterval());
    }

    private Runnable updateToggles() {
        return () -> {
            try {
                FeatureToggleResponse response = toggleFetcher.fetchToggles();
                if (response.getStatus() == FeatureToggleResponse.Status.CHANGED) {
                    toggleCollection = response.getToggleCollection();
                    toggleBackupHandler.write(response.getToggleCollection());
                } else {
                    LOG.warn("Error fetching toggles from Unleash API (StatusCode: {})", response.getHttpStatusCode());
                }
            } catch (UnleashException e) {
                LOG.warn("Could not refresh feature toggles", e);
            }
        };
    }

    @Override
    public FeatureToggle getToggle(String name) {
        return toggleCollection.getToggle(name);
    }

    @Override
    public List<String> getFeatureNames() {
        return toggleCollection.getFeatures().stream().map(toggle -> toggle.getName()).collect(Collectors.toList());
    }
}
