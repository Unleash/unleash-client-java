package io.getunleash.util;

import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;

public class EventDispatcherExceptionHandler implements ExceptionHandler {

    private EventDispatcher eventDispatcher;

    public EventDispatcherExceptionHandler(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void handle(UnleashException e) {
        this.eventDispatcher.dispatch(e);
    }
}
