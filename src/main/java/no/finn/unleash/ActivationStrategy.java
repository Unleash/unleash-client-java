package no.finn.unleash;

import java.util.Map;

public final class ActivationStrategy {
    private final String name;
    private final Map<String, String> parameters;

    ActivationStrategy(String name, Map<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
