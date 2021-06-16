package io.getunleash;

import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.lang.Nullable;

public class UnleashException extends RuntimeException implements UnleashEvent {

    public UnleashException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.onError(this);
    }
}
