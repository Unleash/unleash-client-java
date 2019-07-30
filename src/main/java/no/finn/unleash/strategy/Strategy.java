package no.finn.unleash.strategy;

import java.util.List;
import java.util.Map;

import no.finn.unleash.Constraint;
import no.finn.unleash.UnleashContext;

public interface Strategy {
    String getName();

    boolean isEnabled(Map<String, String> parameters);

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext, List<Constraint> constraints) {
        return ConstraintUtil.validate(constraints, unleashContext) && isEnabled(parameters, unleashContext);
    }
}
