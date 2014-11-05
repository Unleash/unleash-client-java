package no.finn.unleash;

import java.util.Map;

public final class FeatureToggle {
    private final String name;
    private final boolean enabled;
    private final String strategy;
    private final Map<String, String> parameters;

    public FeatureToggle(String name, boolean enabled, String strategy, Map<String, String> parameters) {
        this.name = name;
        this.enabled = enabled;
        this.strategy = strategy;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getStrategy() {
        return strategy;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "FeatureToggle{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", strategy='" + strategy + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
