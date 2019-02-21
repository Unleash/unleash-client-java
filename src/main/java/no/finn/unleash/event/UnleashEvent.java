package no.finn.unleash.event;

public interface UnleashEvent {

    void publishTo(UnleashSubscriber unleashSubscriber);

}
