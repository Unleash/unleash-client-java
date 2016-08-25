package no.finn.unleash;

import java.util.List;

public final class FeatureToggle {
    private final String name;
    private final boolean enabled;
    private final List<ActivationStrategy> strategies;

    public FeatureToggle(String name, boolean enabled, List<ActivationStrategy> strategies) {
        this.name = name;
        this.enabled = enabled;
        this.strategies = strategies;
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

    @Override
    public String toString() {
        return "FeatureToggle{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", strategies='" + strategies + '\'' +
                '}';
    }
}
