package io.getunleash.example;

import java.util.Map;

import io.getunleash.strategy.Strategy;

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
