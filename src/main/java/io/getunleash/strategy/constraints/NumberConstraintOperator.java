package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class NumberConstraintOperator implements ConstraintOperator {
    @Override
    public boolean evaluate(Constraint constraint, UnleashContext context) {
        return context.getByName(constraint.getContextName()).map(cVal -> {
            try {
                return Double.parseDouble(cVal);
            } catch (NumberFormatException nfe) {
                return null;
            }
        }).map(cVal -> {
            List<Double> values = constraint.getValues().stream().map(constraintVal -> {
                try {
                    return Double.parseDouble(constraintVal);
                } catch (NumberFormatException nfe) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return eval(constraint.getOperator(), context, values, cVal);
        }).orElse(false);
    }

    private boolean eval(Operator operator, UnleashContext context, List<Double> values, Double contextValue) {

        switch (operator) {
            case NUM_LT:
                return values.stream().anyMatch(v -> contextValue < v);
            case NUM_LTE:
                return values.stream().anyMatch(v -> contextValue <= v);
            case NUM_EQ:
                return values.stream().anyMatch(contextValue::equals);
            case NUM_GTE:
                return values.stream().anyMatch(v -> contextValue >= v);
            case NUM_GT:
                return values.stream().anyMatch(v -> contextValue > v);
            default:
                return false;
        }
    }
}
