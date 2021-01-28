package no.finn.unleash.event;

import no.finn.unleash.util.UnleashConfig;

public class UnleashConfigured implements UnleashEvent {
    private final UnleashConfig config;
    public UnleashConfigured(UnleashConfig config) {
        this.config = config;
    }
    public UnleashConfig getConfig() {
        return config;
    }
    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.onConfigured(this);
    }
}
