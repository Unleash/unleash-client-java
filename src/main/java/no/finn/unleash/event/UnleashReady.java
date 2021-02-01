package no.finn.unleash.event;

public class UnleashReady implements UnleashEvent {

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.onReady(this);
    }
}
