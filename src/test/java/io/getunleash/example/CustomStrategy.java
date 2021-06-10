package io.getunleash.example;

import io.getunleash.strategy.Strategy;
import java.util.Map;

final class CustomStrategy implements Strategy {
    @Override
    public String getName() {
        return "custom";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }
}
