package io.getunleash.strategy;

import java.util.List;
import java.util.Map;
import io.getunleash.Constraint;
import io.getunleash.UnleashContext;

public interface Strategy {
    String getName();

    boolean isEnabled(Map<String, String> parameters);

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }

    default boolean isEnabled(
            Map<String, String> parameters,
            UnleashContext unleashContext,
            List<Constraint> constraints) {
        return ConstraintUtil.validate(constraints, unleashContext)
                && isEnabled(parameters, unleashContext);
    }
}
