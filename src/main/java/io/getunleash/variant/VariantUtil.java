package io.getunleash.variant;

import io.getunleash.FeatureToggle;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.getunleash.lang.Nullable;
import io.getunleash.strategy.StrategyUtils;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Predicate;

public final class VariantUtil {
    static final String GROUP_ID_KEY = "groupId";

    private VariantUtil() {}

    private static Predicate<VariantOverride> overrideMatchesContext(UnleashContext context) {
        return (override) -> {
            Optional<String> contextValue;
            switch (override.getContextName()) {
                case "userId":
                    {
                        contextValue = context.getUserId();
                        break;
                    }
                case "sessionId":
                    {
                        contextValue = context.getSessionId();
                        break;
                    }
                case "remoteAddress":
                    {
                        contextValue = context.getRemoteAddress();
                        break;
                    }
                default:
                    contextValue =
                            Optional.ofNullable(
                                    context.getProperties().get(override.getContextName()));
                    break;
            }
            return override.getValues().contains(contextValue.orElse(""));
        };
    }

    private static Optional<VariantDefinition> getOverride(
            List<VariantDefinition> variants, UnleashContext context) {
        return variants.stream()
                .filter(
                        variant ->
                                variant.getOverrides().stream()
                                        .anyMatch(overrideMatchesContext(context)))
                .findFirst();
    }

    private static String getIdentifier(UnleashContext context) {
        return context.getUserId()
                .orElse(
                        context.getSessionId()
                                .orElse(
                                        context.getRemoteAddress()
                                                .orElse(Double.toString(Math.random()))));
    }

    private static String randomString() {
        int randSeed = new Random().nextInt(100000);
        return "" + randSeed;
    }

    private static String getSeed(UnleashContext unleashContext, Optional<String> stickiness) {
        return stickiness
                .map(s -> unleashContext.getByName(s).orElse(randomString()))
                .orElse(getIdentifier(unleashContext));
    }

    public static Variant selectVariant(
            @Nullable FeatureToggle featureToggle, UnleashContext context, Variant defaultVariant) {
        if (featureToggle == null) {
            return defaultVariant;
        }

        Variant variant =
                selectVariant(
                        Collections.singletonMap("groupId", featureToggle.getName()),
                        featureToggle.getVariants(),
                        context);

        return variant != null ? variant : defaultVariant;
    }

    public static @Nullable Variant selectVariant(
            Map<String, String> parameters,
            @Nullable List<VariantDefinition> variants,
            UnleashContext context) {
        if (variants != null) {
            int totalWeight = variants.stream().mapToInt(VariantDefinition::getWeight).sum();
            if (totalWeight <= 0) {
                return null;
            }
            Optional<VariantDefinition> variantOverride = getOverride(variants, context);
            if (variantOverride.isPresent()) {
                return variantOverride.get().toVariant();
            }

            Optional<String> customStickiness =
                    variants.stream()
                            .filter(
                                    f ->
                                            f.getStickiness() != null
                                                    && !"default".equals(f.getStickiness()))
                            .map(VariantDefinition::getStickiness)
                            .findFirst();
            int target =
                    StrategyUtils.getNormalizedNumber(
                            getSeed(context, customStickiness),
                            parameters.get(GROUP_ID_KEY),
                            totalWeight);

            int counter = 0;
            for (VariantDefinition variant : variants) {
                if (variant.getWeight() != 0) {
                    counter += variant.getWeight();
                    if (counter >= target) {
                        return variant.toVariant();
                    }
                }
            }
        }
        return null;
    }
}
