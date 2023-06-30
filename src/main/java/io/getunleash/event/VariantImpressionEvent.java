package io.getunleash.event;

import io.getunleash.UnleashContext;

public class VariantImpressionEvent extends ImpressionEvent {
    private String variantName;

    public String getVariantName() {
        return variantName;
    }

    public VariantImpressionEvent(
            String featureName, boolean enabled, UnleashContext context, String variantName) {
        super(featureName, enabled, context);
        this.variantName = variantName;
    }
}
