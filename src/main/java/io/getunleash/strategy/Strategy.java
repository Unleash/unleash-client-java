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

    default FeatureEvaluationResult getResult(
            Map<String, String> parameters,
            UnleashContext unleashContext,
            List<Constraint> constraints,
            @Nullable List<VariantDefinition> variants) {
        return new FeatureEvaluationResult(
                isEnabled(parameters, unleashContext, constraints),
                VariantUtil.selectVariant(parameters, variants, unleashContext));
    }

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
