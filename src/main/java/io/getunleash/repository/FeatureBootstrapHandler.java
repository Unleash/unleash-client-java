package io.getunleash.repository;

import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.util.UnleashConfig;
import java.util.Optional;

public class FeatureBootstrapHandler {
    private final EventDispatcher eventDispatcher;
    private final ToggleBootstrapProvider toggleBootstrapProvider;

    public FeatureBootstrapHandler(UnleashConfig unleashConfig) {
        if (unleashConfig.getToggleBootstrapProvider() != null) {
            this.toggleBootstrapProvider = unleashConfig.getToggleBootstrapProvider();
        } else {
            this.toggleBootstrapProvider = new ToggleBootstrapFileProvider();
        }
        this.eventDispatcher = new EventDispatcher(unleashConfig);
    }

    public Optional<String> read() {
        if (toggleBootstrapProvider != null) {
            String toggleBootstrap = toggleBootstrapProvider.read();
            eventDispatcher.dispatch(new FeatureBootstrapRead(toggleBootstrap));
            return Optional.of(toggleBootstrap);
        }
        return Optional.empty();
    }

    public static class FeatureBootstrapRead implements UnleashEvent {
        private final String featureCollection;

        public FeatureBootstrapRead(String featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.featuresBootstrapped(featureCollection);
        }
    }
}
