package no.finntech.unleash.strategy;

import java.util.Map;

public final class UnknownStrategy implements Strategy {
    public static final String NAME = "unknown";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }
}
