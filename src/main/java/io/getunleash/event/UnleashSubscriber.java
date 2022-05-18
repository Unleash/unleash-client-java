package io.getunleash.event;

import io.getunleash.UnleashException;
import io.getunleash.metric.ClientMetrics;
import io.getunleash.metric.ClientRegistration;
import io.getunleash.repository.FeatureCollection;
import io.getunleash.repository.FeatureToggleResponse;
import io.getunleash.repository.ToggleCollection;
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

    default void featuresBootstrapped(FeatureCollection featureCollection) {}

    default void featuresBackedUp(FeatureCollection featureCollection) {}

    default void featuresBackupRestored(FeatureCollection featureCollection) {}
}
