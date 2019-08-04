package no.finn.unleash;

import java.util.List;

public class Constraint {
    private final String contextName;
    private final Operator operator;
    private final List<String> values;

    public Constraint(String contextName, Operator operator, List<String> values) {
        this.contextName = contextName;
        this.operator = operator;
        this.values = values;
    }

    public String getContextName() {
        return contextName;
    }

    public Operator getOperator() {
        return operator;
    }

    public List<String> getValues() {
        return values;
    }
}
