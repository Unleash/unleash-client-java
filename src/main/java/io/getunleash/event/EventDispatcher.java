package io.getunleash.event;

import io.getunleash.UnleashContext;
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

    // The per-evaluation dispatch methods take the event's fields rather than the event, so the
    // event is only constructed when something will receive it and the evaluation path stays free.

    public void dispatchToggleEvaluated(String toggleName, boolean enabled) {
        if (subscribedToToggleEvaluated) {
            schedule(new ToggleEvaluated(toggleName, enabled));
        }
    }

    public void dispatchIsEnabledImpression(
            String toggleName, boolean enabled, UnleashContext context) {
        if (subscribedToImpressions) {
            schedule(new IsEnabledImpressionEvent(toggleName, enabled, context));
        }
    }

    public void dispatchVariantImpression(
            String toggleName, boolean enabled, UnleashContext context, String variantName) {
        if (subscribedToImpressions) {
            schedule(new VariantImpressionEvent(toggleName, enabled, context, variantName));
        }
    }

    public void dispatch(UnleashEvent unleashEvent) {
        if (!shouldDispatch(unleashEvent)) {
            return;
        }
        schedule(unleashEvent);
    }

    private void schedule(UnleashEvent unleashEvent) {
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
