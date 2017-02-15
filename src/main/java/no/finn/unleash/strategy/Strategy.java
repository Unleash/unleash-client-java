package no.finn.unleash.strategy;

import java.util.Map;
import no.finn.unleash.UnleashContext;

public interface Strategy {
    String getName();

    boolean isEnabled(Map<String, String> parameters);

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }
}
