package no.finn.unleash;

import static java.util.Collections.emptyList;

import java.util.Collections;
import java.util.List;
import no.finn.unleash.lang.Nullable;
import no.finn.unleash.variant.VariantDefinition;

public final class FeatureToggle {
    private final String name;
    private final boolean enabled;
    private final List<ActivationStrategy> strategies;
    @Nullable private final List<VariantDefinition> variants;

    public FeatureToggle(String name, boolean enabled, List<ActivationStrategy> strategies) {
        this(name, enabled, strategies, emptyList());
    }

    public FeatureToggle(
            String name,
            boolean enabled,
            List<ActivationStrategy> strategies,
            @Nullable List<VariantDefinition> variants) {
        this.name = name;
        this.enabled = enabled;
        this.strategies = strategies;
        this.variants = variants;
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
                + '\''
                + '}';
    }
}
