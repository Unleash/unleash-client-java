package io.getunleash;

import static java.util.Collections.emptyList;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.VariantDefinition;
import java.util.Collections;
import java.util.List;

public final class FeatureToggle {
    private final String name;
    private final boolean enabled;
    private final List<ActivationStrategy> strategies;
    @Nullable private final List<VariantDefinition> variants;
    private final boolean impressionData;

    public FeatureToggle(String name, boolean enabled, List<ActivationStrategy> strategies) {
        this(name, enabled, strategies, emptyList(), false);
    }

    public FeatureToggle(
            String name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            List<VariantDefinition> variants) {
        this(name, enabled, strategies, variants, false);
    }

    public FeatureToggle(
            String name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            @Nullable List<VariantDefinition> variants,
            @Nullable Boolean impressionData) {
        this.name = name;
        this.enabled = enabled;
        this.strategies = strategies;
        this.variants = variants;
        this.impressionData = impressionData != null ? impressionData : false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<ActivationStrategy> getStrategies() {
        return this.strategies;
    }

    public List<VariantDefinition> getVariants() {
        if (variants == null) {
            return Collections.emptyList();
        } else {
            return variants;
        }
    }

    @Nullable
    public boolean hasImpressionData() {
        return impressionData;
    }

    @Override
    public String toString() {
        return "FeatureToggle{"
                + "name='"
                + name
                + '\''
                + ", enabled="
                + enabled
                + ", strategies='"
                + strategies
                + '\''
                + ", variants='"
                + variants
                + ", impressionData="
                + impressionData
                + '\''
                + '}';
    }
}
