package no.finn.unleash;

import java.util.List;

import no.finn.unleash.strategy.Variant;

public interface Unleash {
    boolean isEnabled(String toggleName);

    boolean isEnabled(String toggleName, boolean defaultSetting);

    default boolean isEnabled(String toggleName, UnleashContext context) {
        return isEnabled(toggleName, context, false);
    }

    default boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting) {
        return isEnabled(toggleName, defaultSetting);
    }

    Variant getVariant(final String toggleName, final UnleashContext context);
    Variant getVariant(final String toggleName, final UnleashContext context, final String defaultPayload);

    List<String> getFeatureToggleNames();
}
