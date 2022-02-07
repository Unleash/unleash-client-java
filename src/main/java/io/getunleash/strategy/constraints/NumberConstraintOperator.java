package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
                                Double value = Double.parseDouble(
                                    constraint.getValue()
                                );
                                return eval(constraint.getOperator(), context, value, cVal);
                            } catch (NumberFormatException nfe) {
                                return null;
                            }
                        })
                .orElse(false);
    }

    private boolean eval(
            Operator operator, UnleashContext context, Double value, Double contextValue) {

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
