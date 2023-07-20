package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.VariantDefinition;

public class FeatureEvaluationResult {
    private boolean enabled;

    private Variant variant;

    public FeatureEvaluationResult() {
    }

    public FeatureEvaluationResult(boolean enabled, @Nullable Variant variant) {
        this.enabled = enabled;
        this.variant = variant;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
}
