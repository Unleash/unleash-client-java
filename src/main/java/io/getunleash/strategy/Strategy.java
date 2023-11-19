package io.getunleash.strategy;

import io.getunleash.UnleashContext;

import java.util.Map;

public interface Strategy {

    String getName();

    boolean isEnabled(Map<String, String> parameters);

    default boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return isEnabled(parameters);
    }

}
