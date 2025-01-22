package io.getunleash.strategy;

import java.util.Map;

import io.getunleash.UnleashContext;

public interface Strategy {

        String getName();

        boolean isEnabled(Map<String, String> parameters, UnleashContext context);
}
