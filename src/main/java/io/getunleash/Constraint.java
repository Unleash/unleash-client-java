package io.getunleash;

import java.util.List;

public class Constraint {
    private final String contextName;
    private final Operator operator;
    private final List<String> values;
    private final boolean inverted;
    private final boolean caseInsensitive;

    public Constraint(String contextName, Operator operator, List<String> values) {
        this(contextName, operator, values, false, false);
    }

    public Constraint(String contextName, Operator operator, List<String> values, boolean inverted) {
        this(contextName, operator, values, inverted, false);

    }

    public Constraint(
            String contextName,
            Operator operator,
            List<String> values,
            boolean inverted,
            boolean caseInsensitive) {
        this.contextName = contextName;
        this.operator = operator;
        this.values = values;
        this.inverted = inverted;
        this.caseInsensitive = caseInsensitive;
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

    public boolean isInverted() {
        return inverted;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }
}
