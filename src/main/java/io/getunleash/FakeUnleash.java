package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.Variant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class FakeUnleash implements Unleash {
    private boolean enableAll = false;
    private boolean disableAll = false;
    private final Map<String, Boolean> excludedFeatures = new ConcurrentHashMap<>();
    /**
     * @implNote This uses {@link Queue} instead of {@link List}, as there are concurrent queues,
     *     but no concurrent lists in the jdk.
     */
    private final Map<String, Queue<FakeContextMatcher>> features = new ConcurrentHashMap<>();

    private final Map<String, Variant> variants = new ConcurrentHashMap<>();

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        if (enableAll) {
            return excludedFeatures.getOrDefault(toggleName, true);
        } else if (disableAll) {
            return excludedFeatures.getOrDefault(toggleName, false);
        } else {
            Queue<FakeContextMatcher> fakeContextMatchers = features.get(toggleName);
            if (fakeContextMatchers == null) {
                return defaultSetting;
            } else {
                return fakeContextMatchers.stream()
                        .anyMatch(fakeContextMatcher -> fakeContextMatcher.matches(null));
            }
        }
    }

    @Override
    public boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction) {
        if (!enableAll && !disableAll || excludedFeatures.containsKey(toggleName)) {
            Queue<FakeContextMatcher> fakeContextMatchers = features.get(toggleName);
            if (fakeContextMatchers == null) {
                return fallbackAction.test(toggleName, UnleashContext.builder().build());
            } else {
                return fakeContextMatchers.stream()
                        .anyMatch(fakeContextMatcher -> fakeContextMatcher.matches(context));
            }
        }
        return isEnabled(toggleName);
    }

    @Override
    public boolean isEnabled(
            String toggleName, BiPredicate<String, UnleashContext> fallbackAction) {
        if ((!enableAll && !disableAll || excludedFeatures.containsKey(toggleName))
                && !features.containsKey(toggleName)) {
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
            this.features.put(name, only(FakeContextMatcher.ALWAYS));
        }
    }

    public void disable(String... features) {
        for (String name : features) {
            this.features.put(name, only(FakeContextMatcher.NEVER));
        }
    }

    public void reset(String... features) {
        for (String name : features) {
            this.features.remove(name);
        }
    }

    /**
     * Enables or disables feature toggles depending on the evaluation of the {@link
     * FakeContextMatcher contextMatcher}. This can be called multiple times, and the combined state
     * will be examined. This lets you conditionally configure multiple different tests to do
     * different things, while running concurrently. This will be overwritten if {@link
     * #enable(String...)} or {@link #disable(String...)} are called.
     *
     * @param contextMatcher the context matcher to evaluate
     * @param features the features for which the context matcher will be invoked
     */
    public void conditionallyEnable(FakeContextMatcher contextMatcher, String... features) {
        for (String name : features) {
            this.features.computeIfAbsent(name, ignored -> new ArrayDeque<>()).add(contextMatcher);
        }
    }

    private static Queue<FakeContextMatcher> only(FakeContextMatcher fakeContextMatcher) {
        Queue<FakeContextMatcher> only = new ArrayDeque<>();
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

    /**
     * Matches a context provided to {@link Unleash#isEnabled(String, UnleashContext)} and related
     * methods with a context provided to {@link FakeUnleash#conditionallyEnable(FakeContextMatcher,
     * String...)} and related methods.
     */
    public interface FakeContextMatcher {
        /**
         * Always matches any context provided. This is useful when enabling a feature toggle
         * regardless of the context.
         */
        FakeContextMatcher ALWAYS = context -> true;
        /**
         * Never matches any context provided. This is useful when disabling a feature toggle
         * regardless of the context.
         */
        FakeContextMatcher NEVER = context -> false;

        /**
         * If all you want to do is match a provided context exactly, this is a shortcut method to
         * do just that.
         *
         * @param expectedContext the context you want to match
         * @return a matcher that returns {@code true} iff the {@code expectedContext} is equal to
         *     the {@code actualContext}. If no context is passed, such as when using {@link
         *     Unleash#isEnabled(String)}, this method will return {@code false}.
         */
        static FakeContextMatcher equals(UnleashContext expectedContext) {
            return new FakeContextMatcher() {
                @Override
                public boolean matches(@Nullable UnleashContext actualContext) {
                    if (actualContext == null) {
                        return false;
                    }
                    return Objects.equals(
                            expectedContext.getProperties(), actualContext.getProperties());
                }
            };
        }

        /**
         * Evaluate if the context provided to {@link Unleash#isEnabled(String, UnleashContext)} and
         * related methods matches the conditions you set. This is for more complex configurations
         * than what {@link #equals(UnleashContext)} offers.
         *
         * @param context the context passed to {@link Unleash#isEnabled(String, UnleashContext)}
         *     and related methods. This is {@code null} is no context was provided, such as when
         *     calling {@link Unleash#isEnabled(String)}
         * @return {@code true} iff {@code context} matches the check, otherwise {@code false}.
         */
        boolean matches(@Nullable UnleashContext context);
    }
}
