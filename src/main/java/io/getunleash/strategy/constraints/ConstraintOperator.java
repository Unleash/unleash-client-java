package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.UnleashContext;

public interface ConstraintOperator {
    boolean evaluate(Constraint constraint, UnleashContext context);
}
