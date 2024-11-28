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
        String strategyStickiness = getStickiness(parameters);
        return new FeatureEvaluationResult(
                enabled,
                enabled
                        ? VariantUtil.selectVariant(
                                parameters, variants, unleashContext, strategyStickiness)
                        : null);
    }

    /**
     * Uses the old pre 9.0.0 way of hashing for finding the Variant to return
     *
     * @deprecated
     * @param parameters
     * @param unleashContext
     * @param constraints
     * @param variants
     * @return
     */
    default FeatureEvaluationResult getDeprecatedHashingAlgoResult(
            Map<String, String> parameters,
            UnleashContext unleashContext,
            List<Constraint> constraints,
            @Nullable List<VariantDefinition> variants) {
        boolean enabled = isEnabled(parameters, unleashContext, constraints);
        return new FeatureEvaluationResult(
                enabled,
                enabled
                        ? VariantUtil.selectDeprecatedVariantHashingAlgo(
                                parameters, variants, unleashContext)
                        : null);
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

    default String getStickiness(@Nullable Map<String, String> parameters) {
        if (parameters != null) {
            return parameters.getOrDefault("stickiness", "default");
        }
        return null;
    }
}
