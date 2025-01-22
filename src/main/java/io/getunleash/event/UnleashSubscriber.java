package io.getunleash.event;

import io.getunleash.UnleashException;
import io.getunleash.metric.ClientMetrics;
import io.getunleash.metric.ClientRegistration;
import org.slf4j.LoggerFactory;

public interface UnleashSubscriber {

    default void onError(UnleashException unleashException) {
        LoggerFactory.getLogger(UnleashSubscriber.class)
                .warn(unleashException.getMessage(), unleashException);
    }

    default void on(UnleashEvent unleashEvent) {}

    default void onReady(UnleashReady unleashReady) {}

    default void toggleEvaluated(ToggleEvaluated toggleEvaluated) {}

    default void togglesFetched(ClientFeaturesResponse toggleResponse) {}

    default void clientMetrics(ClientMetrics clientMetrics) {}

    default void clientRegistered(ClientRegistration clientRegistration) {}

    default void featuresBootstrapped(FeatureSet featureCollection) {}

    default void featuresBackedUp(FeatureSet featureCollection) {}

    default void featuresBackupRestored(FeatureSet featureCollection) {}

    default void impression(ImpressionEvent impressionEvent) {}
}
