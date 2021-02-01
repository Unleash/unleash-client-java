package no.finn.unleash;

import no.finn.unleash.event.UnleashEvent;
import no.finn.unleash.event.UnleashSubscriber;

public class UnleashException extends RuntimeException implements UnleashEvent {

    public UnleashException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.onError(this);
    }
}
