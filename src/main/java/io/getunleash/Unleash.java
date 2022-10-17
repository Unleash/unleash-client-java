package io.getunleash;

import java.util.List;
import java.util.function.BiPredicate;

public interface Unleash {
    boolean isEnabled(String toggleName);

    boolean isEnabled(String toggleName, boolean defaultSetting);

    default boolean isEnabled(String toggleName, UnleashContext context) {
        return isEnabled(toggleName, context, false);
    }

    default boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting) {
        return isEnabled(toggleName, defaultSetting);
    }

    default boolean isEnabled(
            String toggleName, BiPredicate<String, UnleashContext> fallbackAction) {
        return isEnabled(toggleName, false);
    }

    default boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction) {
        return isEnabled(toggleName, context, false);
    }

    Variant getVariant(final String toggleName, final UnleashContext context);

    Variant getVariant(
            final String toggleName, final UnleashContext context, final Variant defaultValue);

    Variant getVariant(final String toggleName);

    Variant getVariant(final String toggleName, final Variant defaultValue);

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
