package io.getunleash;

import static java.util.Collections.emptyList;

import io.getunleash.lang.Nullable;
import io.getunleash.strategy.*;
import io.getunleash.variant.VariantUtil;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class FakeUnleash implements Unleash {
    private boolean enableAll = false;
    private boolean disableAll = false;
    private Map<String, Boolean> excludedFeatures = new HashMap<>();
    private Map<String, FeatureToggle> features = new HashMap<>();
    private Map<String, Variant> variants = new HashMap<>();

    private static final List<Strategy> BUILTIN_STRATEGIES =
            Arrays.asList(
                    new DefaultStrategy(),
                    new ApplicationHostnameStrategy(),
                    new GradualRolloutRandomStrategy(),
                    new GradualRolloutSessionIdStrategy(),
                    new GradualRolloutUserIdStrategy(),
                    new RemoteAddressStrategy(),
                    new UserWithIdStrategy(),
                    new FlexibleRolloutStrategy());

    private FeatureEvaluationResult getFeatureEvaluationResult(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction,
            @Nullable Variant defaultVariant) {
        Optional<ActivationStrategy> enabledStrategy = Optional.empty();
        boolean enabled = isEnabled(toggleName, context, fallbackAction);
        if (features.containsKey(toggleName)) {
            enabledStrategy =
                    features.get(toggleName).getStrategies().stream()
                            .filter(
                                    s -> {
                                        Optional<Strategy> strategy =
                                                BUILTIN_STRATEGIES.stream()
                                                        .filter(
                                                                strategy1 ->
                                                                        strategy1
                                                                                .getName()
                                                                                .equals(
                                                                                        s
                                                                                                .getName()))
                                                        .findFirst();

                                        return strategy.map(
                                                        value ->
                                                                value.isEnabled(
                                                                        s.getParameters(),
                                                                        context,
                                                                        s.getConstraints()))
                                                .orElse(false);
                                    })
                            .findFirst();
        }
        FeatureEvaluationResult result = new FeatureEvaluationResult(enabled, null);
        if (enabledStrategy.isPresent()) {
            Optional<Strategy> strategy =
                    BUILTIN_STRATEGIES.stream()
                            .filter(strategy1 -> strategy1.getName().equals(strategy1.getName()))
                            .findFirst();

            Strategy configuredStrategy = strategy.get();
            result =
                    configuredStrategy.getResult(
                            enabledStrategy.get().getParameters(),
                            context,
                            enabledStrategy.get().getConstraints(),
                            enabledStrategy.get().getVariants());
        }

        Variant variant = result.isEnabled() ? result.getVariant() : null;
        // If strategy variant is null, look for a variant in the featureToggle
        if (variant == null && defaultVariant != null) {
            variant =
                    result.isEnabled()
                            ? VariantUtil.selectVariant(
                                    features.get(toggleName), context, defaultVariant)
                            : defaultVariant;
        }
        result.setVariant(variant);

        return result;
    }

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        if (enableAll) {
            return excludedFeatures.getOrDefault(toggleName, true);
        } else if (disableAll) {
            return excludedFeatures.getOrDefault(toggleName, false);
        } else {
            return more().getFeatureToggleDefinition(toggleName)
                    .map(FeatureToggle::isEnabled)
                    .orElse(defaultSetting);
        }
    }

    @Override
    public boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction) {
        return isEnabled(toggleName, fallbackAction);
    }

    @Override
    public boolean isEnabled(
            String toggleName, BiPredicate<String, UnleashContext> fallbackAction) {
        if (!features.containsKey(toggleName)) {
            return fallbackAction.test(toggleName, UnleashContext.builder().build());
        }
        return isEnabled(toggleName);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, Variant.DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        return getVariant(toggleName, defaultValue);
    }

    @Override
    public Variant getVariant(String toggleName) {
        return getVariant(toggleName, Variant.DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        if (isEnabled(toggleName) && variants.containsKey(toggleName)) {
            return variants.get(toggleName);
        } else {
            return defaultValue;
        }
    }

    @Override
    public List<String> getFeatureToggleNames() {
        return more().getFeatureToggleNames();
    }

    @Override
    public MoreOperations more() {
        return new FakeMore();
    }

    public void enableAll() {
        disableAll = false;
        enableAll = true;
        excludedFeatures.clear();
        features.clear();
    }

    public void enableAllExcept(String... excludedFeatures) {
        enableAll();
        for (String toggle : excludedFeatures) {
            this.excludedFeatures.put(toggle, false);
        }
    }

    public void disableAll() {
        disableAll = true;
        enableAll = false;
        excludedFeatures.clear();
        features.clear();
    }

    public void disableAllExcept(String... excludedFeatures) {
        disableAll();
        for (String toggle : excludedFeatures) {
            this.excludedFeatures.put(toggle, true);
        }
    }

    public void resetAll() {
        disableAll = false;
        enableAll = false;
        excludedFeatures.clear();
        features.clear();
        variants.clear();
    }

    public void enable(String... features) {
        for (String name : features) {
            this.features.put(name, new FeatureToggle(name, true, emptyList()));
        }
    }

    public void disable(String... features) {
        for (String name : features) {
            this.features.put(name, new FeatureToggle(name, false, emptyList()));
        }
    }

    public void reset(String... features) {
        for (String name : features) {
            this.features.remove(name);
        }
    }

    public void setVariant(String t1, Variant a) {
        variants.put(t1, a);
    }

    public class FakeMore implements MoreOperations {

        @Override
        public List<String> getFeatureToggleNames() {
            return new ArrayList<>(features.keySet());
        }

        @Override
        public Optional<FeatureToggle> getFeatureToggleDefinition(String toggleName) {
            return Optional.ofNullable(features.get(toggleName));
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles() {
            return evaluateAllToggles(null);
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles(@Nullable UnleashContext context) {
            return getFeatureToggleNames().stream()
                    .map(
                            toggleName ->
                                    new EvaluatedToggle(
                                            toggleName,
                                            isEnabled(toggleName),
                                            getVariant(toggleName)))
                    .collect(Collectors.toList());
        }

        @Override
        public void count(String toggleName, boolean enabled) {
            // Nothing to count
        }

        @Override
        public void countVariant(String toggleName, String variantName) {
            // Nothing to count
        }
    }
}
