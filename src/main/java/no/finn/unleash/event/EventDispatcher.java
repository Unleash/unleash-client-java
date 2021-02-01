package no.finn.unleash.event;

import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;

public class EventDispatcher {

    private final UnleashSubscriber unleashSubscriber;
    private final UnleashScheduledExecutor unleashScheduledExecutor;

    public EventDispatcher(UnleashConfig unleashConfig) {
        this.unleashSubscriber = unleashConfig.getSubscriber();
        this.unleashScheduledExecutor = unleashConfig.getScheduledExecutor();
    }

    public void dispatch(UnleashEvent unleashEvent) {
        unleashScheduledExecutor.scheduleOnce(
                () -> {
                    unleashSubscriber.on(unleashEvent);
                    unleashEvent.publishTo(unleashSubscriber);
                });
    }
}
