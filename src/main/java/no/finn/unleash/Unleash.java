package no.finn.unleash;

import java.util.List;
import java.util.function.BiFunction;

public interface Unleash {
    boolean isEnabled(String toggleName);

    boolean isEnabled(String toggleName, boolean defaultSetting);

    default boolean isEnabled(String toggleName, UnleashContext context) {
        return isEnabled(toggleName, context, false);
    }

    default boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting) {
        return isEnabled(toggleName, defaultSetting);
    }

    default boolean isEnabled(final String toggleName, final BiFunction<String, UnleashContext, Boolean> fallbackAction) {
        return isEnabled(toggleName, false, fallbackAction);
    }

    boolean isEnabled(final String toggleName, boolean defaultSetting, final BiFunction<String, UnleashContext, Boolean> fallbackAction);

    boolean isEnabled(final String toggleName, UnleashContext context, boolean defaultSetting, final BiFunction<String, UnleashContext, Boolean> fallbackAction);

    Variant getVariant(final String toggleName, final UnleashContext context);

    Variant getVariant(final String toggleName, final UnleashContext context, final Variant defaultValue);

    Variant getVariant(final String toggleName);

    Variant getVariant(final String toggleName, final Variant defaultValue);

    List<String> getFeatureToggleNames();
}
