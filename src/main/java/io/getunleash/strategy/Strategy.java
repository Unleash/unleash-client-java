package io.getunleash.strategy;

import io.getunleash.Constraint;
import io.getunleash.UnleashContext;

import java.util.List;
import java.util.Map;

public interface Strategy {

    String getName();

    boolean isEnabled(Map<String, String> parameters);

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }

    /** @deprecated constraint validation should be delegated to Yggdrasil */
    @Deprecated
    default boolean isEnabled(
            Map<String, String> parameters,
            UnleashContext unleashContext,
            List<Constraint> constraints) {
        return ConstraintUtil.validate(constraints, unleashContext)
                && isEnabled(parameters, unleashContext);
    }
}
