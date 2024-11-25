package io.getunleash.strategy;

import io.getunleash.Constraint;
import io.getunleash.FeatureEvaluationResult;
import io.getunleash.UnleashContext;
import io.getunleash.lang.Nullable;
import io.getunleash.variant.VariantDefinition;
import io.getunleash.variant.VariantUtil;
import java.util.List;
import java.util.Map;

public interface Strategy {

    String getName();

    boolean isEnabled(Map<String, String> parameters);

    /**
     * @deprecated don't use this method, currently only accessible from tests
     */
    @Deprecated
    default FeatureEvaluationResult getResult(
            Map<String, String> parameters,
            UnleashContext unleashContext,
            List<Constraint> constraints,
            @Nullable List<VariantDefinition> variants) {
        boolean enabled = isEnabled(parameters, unleashContext, constraints);
        return new FeatureEvaluationResult(
                enabled,
                enabled ? VariantUtil.selectVariant(parameters, variants, unleashContext) : null);
    }

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }

    /**
     * @deprecated constraint validation should be delegated to Yggdrasil
     */
    @Deprecated
    default boolean isEnabled(
            Map<String, String> parameters,
            UnleashContext unleashContext,
            List<Constraint> constraints) {
        return ConstraintUtil.validate(constraints, unleashContext)
                && isEnabled(parameters, unleashContext);
    }
}
