package io.getunleash.event;

import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;

public class EventDispatcher {

    private final UnleashSubscriber unleashSubscriber;
    private final UnleashScheduledExecutor unleashScheduledExecutor;

    private final boolean subscribedToToggleEvaluated;
    private final boolean subscribedToImpressions;

    public EventDispatcher(UnleashConfig unleashConfig) {
        this.unleashSubscriber = unleashConfig.getSubscriber();
        this.unleashScheduledExecutor = unleashConfig.getScheduledExecutor();

        Class<?> subscriberType = unleashSubscriber.getClass();
        // Both per-evaluation events also reach the catch-all on(UnleashEvent), so a subscriber
        // that overrides it is subscribed to everything.
        boolean subscribedToAllEvents = overridesCallback(subscriberType, "on", UnleashEvent.class);
        this.subscribedToToggleEvaluated =
                subscribedToAllEvents
                        || overridesCallback(
                                subscriberType, "toggleEvaluated", ToggleEvaluated.class);
        this.subscribedToImpressions =
                subscribedToAllEvents
                        || overridesCallback(subscriberType, "impression", ImpressionEvent.class);
    }

    /**
     * Whether an event of this type would reach anything on the configured subscriber.
     *
     * <p>Callers on the evaluation path use this to skip constructing events nobody will receive.
     * Only the per-evaluation event types can report {@code false}; every other event type always
     * dispatches.
     *
     * @param eventType the concrete event class the caller is about to construct
     * @return false only when the subscriber inherits the no-op default for every callback this
     *     event would reach
     */
    public boolean isSubscribedTo(Class<? extends UnleashEvent> eventType) {
        if (ToggleEvaluated.class.equals(eventType)) {
            return subscribedToToggleEvaluated;
        }
        if (ImpressionEvent.class.isAssignableFrom(eventType)) {
            return subscribedToImpressions;
        }
        return true;
    }

    public void dispatch(UnleashEvent unleashEvent) {
        if (!shouldDispatch(unleashEvent)) {
            return;
        }
        unleashScheduledExecutor.scheduleOnce(
                () -> {
                    unleashSubscriber.on(unleashEvent);
                    unleashEvent.publishTo(unleashSubscriber);
                });
    }

    private boolean shouldDispatch(UnleashEvent unleashEvent) {
        if (unleashEvent instanceof ToggleEvaluated) {
            return subscribedToToggleEvaluated;
        }
        if (unleashEvent instanceof ImpressionEvent) {
            return subscribedToImpressions;
        }
        return true;
    }

    /**
     * True unless the subscriber inherits the no-op default straight from {@link
     * UnleashSubscriber}. Anything we cannot resolve is reported as overridden, so no callback is
     * ever suppressed; generated proxies declare every method and therefore also end up here.
     */
    private static boolean overridesCallback(
            Class<?> subscriberType, String callback, Class<?> eventType) {
        try {
            return subscriberType.getMethod(callback, eventType).getDeclaringClass()
                    != UnleashSubscriber.class;
        } catch (NoSuchMethodException | RuntimeException e) {
            return true;
        }
    }
}
