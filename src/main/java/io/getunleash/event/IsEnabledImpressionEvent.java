package io.getunleash.event;

import io.getunleash.UnleashContext;

public class IsEnabledImpressionEvent extends ImpressionEvent {
    public IsEnabledImpressionEvent(String featureName, boolean enabled, UnleashContext context) {
        super(featureName, enabled, context);
    }
}
