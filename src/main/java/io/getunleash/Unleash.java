package io.getunleash;

import java.util.List;
import java.util.function.BiPredicate;

public interface Unleash {
    default boolean isEnabled(String toggleName) {
        return isEnabled(toggleName, false);
    }

    default boolean isEnabled(String toggleName, boolean defaultSetting) {
        return isEnabled(toggleName, UnleashContext.builder().build(), defaultSetting);
    }

    default boolean isEnabled(String toggleName, UnleashContext context) {
        return isEnabled(toggleName, context, false);
    }

    default boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting) {
        return isEnabled(toggleName, context, (n, c) -> defaultSetting);
    }

    default boolean isEnabled(String toggleName, BiPredicate<String, UnleashContext> fallbackAction) {
        return isEnabled(toggleName, UnleashContext.builder().build(), fallbackAction);
    }

    boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction);

    Variant getVariant(final String toggleName, final UnleashContext context);

    Variant getVariant(
            final String toggleName, final UnleashContext context, final Variant defaultValue);

    default Variant getVariant(final String toggleName) {
        return getVariant(toggleName, UnleashContext.builder().build());
    }

    default Variant getVariant(final String toggleName, final Variant defaultValue) {
        return getVariant(toggleName, UnleashContext.builder().build(), defaultValue);
    }

    /**
     * Use more().getFeatureToggleNames() instead
     *
     * @return a list of known toggle names
     */
    @Deprecated()
    List<String> getFeatureToggleNames();

    default void shutdown() {}

    MoreOperations more();
}
