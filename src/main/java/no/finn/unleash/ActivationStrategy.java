package no.finn.unleash;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ActivationStrategy {
    private final String name;
    private final Map<String, String> parameters;
    private final List<Constraint> constraints;

    public ActivationStrategy(String name, Map<String, String> parameters) {
        this(name, parameters, Collections.emptyList());
    }

    public ActivationStrategy(String name, Map<String, String> parameters, List<Constraint> constraints) {
        this.name = name;
        this.parameters = parameters;
        this.constraints = constraints;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }
}
