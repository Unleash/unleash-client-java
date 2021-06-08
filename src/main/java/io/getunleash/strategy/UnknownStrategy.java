package io.getunleash.strategy;

import java.util.Map;

public final class UnknownStrategy implements Strategy {

    private static final String STRATEGY_NAME = "unknown";

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }
}
