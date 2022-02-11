package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import java.util.Objects;

public class NumberConstraintOperator implements ConstraintOperator {
    @Override
    public boolean evaluate(Constraint constraint, UnleashContext context) {
        return context.getByName(constraint.getContextName())
                .map(
                        cVal -> {
                            try {
                                return Double.parseDouble(cVal);
                            } catch (NumberFormatException nfe) {
                                return null;
                            }
                        })
                .map(
                        cVal -> {
                            try {
                                if (constraint.getValues().size() > 0) {
                                    return constraint.getValues().stream()
                                            .map(
                                                    v -> {
                                                        try {
                                                            return Double.parseDouble(v);
                                                        } catch (NumberFormatException nfe) {
                                                            return null;
                                                        }
                                                    })
                                            .filter(Objects::nonNull)
                                            .anyMatch(v -> eval(constraint.getOperator(), v, cVal));
                                } else if (constraint.getValue() != null
                                        && constraint.getValue().length() > 0) {
                                    Double value = Double.parseDouble(constraint.getValue());
                                    return eval(constraint.getOperator(), value, cVal);
                                } else {
                                    return null;
                                }
                            } catch (NumberFormatException nfe) {
                                return null;
                            }
                        })
                .orElse(false);
    }

    private boolean eval(Operator operator, Double value, Double contextValue) {

        switch (operator) {
            case NUM_LT:
                return contextValue < value;
            case NUM_LTE:
                return contextValue <= value;
            case NUM_EQ:
                return contextValue.equals(value);
            case NUM_GTE:
                return contextValue >= value;
            case NUM_GT:
                return contextValue > value;
            default:
                return false;
        }
    }
}
