package io.getunleash;

import java.util.Collections;
import java.util.List;

public class Constraint {
    private final String contextName;
    private final Operator operator;
    private final List<String> values;
    private final String value;
    private final boolean inverted;
    private final boolean caseInsensitive;

    public Constraint(String contextName, Operator operator, List<String> values) {
        this(contextName, operator, null, values, false, false);
    }
    public Constraint(String contextName, Operator operator, String value) {
        this(contextName, operator, value, Collections.emptyList(), false, false);
    }

    public Constraint(
            String contextName, Operator operator, List<String> values, boolean inverted) {
        this(contextName, operator, null, values, inverted, false);
    }
    public Constraint(String contextName, Operator operator, String value, boolean inverted) {
        this(contextName, operator, value, Collections.emptyList(), inverted, false);
    }

    public Constraint(
            String contextName,
            Operator operator,
            List<String> values,
            boolean inverted,
            boolean caseInsensitive) {
        this(contextName, operator, null, values, inverted, caseInsensitive);
    }

    public Constraint(String contextName, Operator operator, String value, List<String> values, boolean inverted, boolean caseInsensitive) {
        this.contextName = contextName;
        this.operator = operator;
        this.values = values;
        this.inverted = inverted;
        this.caseInsensitive = caseInsensitive;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }
}
