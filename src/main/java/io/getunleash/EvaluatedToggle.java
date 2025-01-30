package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.Variant;

public class EvaluatedToggle {
    private final boolean enabled;
    private final String name;
    @Nullable private final Variant variant;

    public EvaluatedToggle(String name, boolean enabled, @Nullable Variant variant) {
        this.enabled = enabled;
        this.name = name;
        this.variant = variant;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public Variant getVariant() {
        return variant;
    }
}
