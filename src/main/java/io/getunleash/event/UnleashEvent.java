package io.getunleash.event;

public interface UnleashEvent {

    void publishTo(UnleashSubscriber unleashSubscriber);
}
