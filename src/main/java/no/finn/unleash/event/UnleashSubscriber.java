package no.finn.unleash.event;

import no.finn.unleash.UnleashException;
import no.finn.unleash.metric.ClientMetrics;
import no.finn.unleash.metric.ClientRegistration;
import no.finn.unleash.repository.FeatureToggleResponse;
import no.finn.unleash.repository.ToggleCollection;
import org.slf4j.LoggerFactory;

public interface UnleashSubscriber {

    default void onError(UnleashException unleashException) {
        LoggerFactory.getLogger(UnleashSubscriber.class)
                .warn(unleashException.getMessage(), unleashException);
    }

    default void on(UnleashEvent unleashEvent) {}

    default void onReady(UnleashReady unleashReady) {}

    default void toggleEvaluated(ToggleEvaluated toggleEvaluated) {}

    default void togglesFetched(FeatureToggleResponse toggleResponse) {}

    default void clientMetrics(ClientMetrics clientMetrics) {}

    default void clientRegistered(ClientRegistration clientRegistration) {}

    default void togglesBackedUp(ToggleCollection toggleCollection) {}

    default void toggleBackupRestored(ToggleCollection toggleCollection) {}

    default void togglesBootstrapped(ToggleCollection toggleCollection) {}
}
