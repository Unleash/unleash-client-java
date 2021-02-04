package no.finn.unleash;

import no.finn.unleash.event.UnleashEvent;
import no.finn.unleash.event.UnleashSubscriber;
import no.finn.unleash.lang.Nullable;

public class UnleashException extends RuntimeException implements UnleashEvent {

    public UnleashException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.onError(this);
    }
}
