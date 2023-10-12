package io.getunleash;

import static java.util.Collections.emptyList;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.VariantDefinition;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public final class FeatureToggle {
    private final String name;
    private final boolean enabled;
    private final List<ActivationStrategy> strategies;
    @Nullable private final List<VariantDefinition> variants;
    private final boolean impressionData;

    @Nullable private final List<FeatureDependency> dependencies;

    public FeatureToggle(String name, boolean enabled, List<ActivationStrategy> strategies) {
        this(name, enabled, strategies, emptyList(), false, emptyList());
    }

    public FeatureToggle(
            String name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            List<VariantDefinition> variants) {
        this(name, enabled, strategies, variants, false, emptyList());
    }

    public FeatureToggle(
            String name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            @Nullable List<VariantDefinition> variants,
            @Nullable Boolean impressionData) {
        this(name, enabled, strategies, variants, impressionData, emptyList());
    }

    public FeatureToggle(
            String name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            @Nullable List<VariantDefinition> variants,
            @Nullable Boolean impressionData,
            @Nullable List<FeatureDependency> dependencies) {
        this.name = name;
        this.enabled = enabled;
        this.strategies = strategies;
        this.variants = variants;
        this.impressionData = impressionData != null ? impressionData : false;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nonnull
    public List<ActivationStrategy> getStrategies() {
        if (strategies == null) {
            return Collections.emptyList();
        }
        return this.strategies;
    }

    @Nonnull
    public List<VariantDefinition> getVariants() {
        if (variants == null) {
            return Collections.emptyList();
        } else {
            return variants;
        }
    }

    @Nonnull
    public List<FeatureDependency> getDependencies() {
        if (dependencies == null) {
            return Collections.emptyList();
        } else {
            return dependencies;
        }
    }

    public boolean hasDependencies() {
        return dependencies != null && !dependencies.isEmpty();
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
                + ", strategies="
                + strategies
                + ", variants="
                + variants
                + ", impressionData="
                + impressionData
                + ", dependencies="
                + dependencies
                + '}';
    }
}
