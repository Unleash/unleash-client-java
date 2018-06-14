package no.finn.unleash;

import java.util.List;

public interface Unleash {
    boolean isEnabled(String toggleName);

    boolean isEnabled(String toggleName, boolean defaultSetting);

    default boolean isEnabled(String toggleName, UnleashContext context) {
        return isEnabled(toggleName, context, false);
    }

    default boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting) {
        return isEnabled(toggleName, defaultSetting);
    }

    List<String> getFeatureToggleNames();
}
