package io.getunleash.strategy;

import java.util.Map;

public final class DefaultStrategy implements Strategy {

    private static final String STRATEGY_NAME = "default";

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return true;
    }
}
