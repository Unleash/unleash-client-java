package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.Variant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FakeUnleash implements Unleash {
    private boolean enableAll = false;
    private boolean disableAll = false;
    private final Map<String, Boolean> excludedFeatures = new ConcurrentHashMap<>();
    /**
     * @implNote This uses {@link Queue} instead of {@link List}, as there are concurrent queues,
     *     but no concurrent lists in the jdk.
     */
    private final Map<String, Queue<Predicate<UnleashContext>>> features =
            new ConcurrentHashMap<>();

    private final Map<String, Variant> variants = new ConcurrentHashMap<>();

    @Override
    public boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction) {
        if (enableAll) {
            return excludedFeatures.getOrDefault(toggleName, true);
        } else if (disableAll) {
            return excludedFeatures.getOrDefault(toggleName, false);
        } else {
            Queue<Predicate<UnleashContext>> contextMatchers = features.get(toggleName);
            if (contextMatchers == null) {
                return fallbackAction.test(toggleName, context);
            } else {
                return contextMatchers.stream()
                        .anyMatch(fakeContextMatcher -> fakeContextMatcher.test(context));
            }
        }
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
            this.features.put(name, only(context -> true));
        }
    }

    public void disable(String... features) {
        for (String name : features) {
            this.features.put(name, only(context -> false));
        }
    }

    public void reset(String... features) {
        for (String name : features) {
            this.features.remove(name);
        }
    }

    /**
     * Enables or disables feature toggles depending on the evaluation of the {@code
     * contextMatcher}. This can be called multiple times. If <b>any</b> of the context matchers
     * match, the feature is enabled. This lets you conditionally configure multiple different tests
     * to do different things, while running concurrently. This will be overwritten if {@link
     * #enable(String...)} or {@link #disable(String...)} are called.
     *
     * @param contextMatcher the context matcher to evaluate
     * @param features the features for which the context matcher will be invoked
     */
    public void conditionallyEnable(Predicate<UnleashContext> contextMatcher, String... features) {
        for (String name : features) {
            this.features.computeIfAbsent(name, ignored -> new ArrayDeque<>()).add(contextMatcher);
        }
    }

    private static Queue<Predicate<UnleashContext>> only(
            Predicate<UnleashContext> fakeContextMatcher) {
        Queue<Predicate<UnleashContext>> only = new ArrayDeque<>();
        only.add(fakeContextMatcher);
        return only;
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
        public Optional<FeatureDefinition> getFeatureToggleDefinition(String toggleName) {
            return Optional.ofNullable(features.get(toggleName))
                    .map(
                            value ->
                                    new FeatureDefinition(
                                            toggleName,
                                            Optional.of("experiment"),
                                            "default",
                                            true));
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
    }
}
