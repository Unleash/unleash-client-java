package no.finn.unleash;

import java.util.List;
import java.util.Map;

public final class ActivationStrategy {
    private final String name;
    private final Map<String, String> parameters;

    //TODO; 1. Should probably have a special GroupStrategy for groups
    //TODO: 2. could use enum for operator
    private final String operator;
    private final List<ActivationStrategy> group;

    public ActivationStrategy(String name, Map<String, String> parameters) {
        this.name = name;
        this.parameters = parameters;
        this.operator = null;
        this.group = null;
    }

    public ActivationStrategy(String name, Map<String, String> parameters, String operator, List<ActivationStrategy> group) {
        this.name = name;
        this.parameters = parameters;
        this.operator = operator;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getOperator() {
        return operator;
    }

    public List<ActivationStrategy> getGroup() {
        return group;
    }
}
