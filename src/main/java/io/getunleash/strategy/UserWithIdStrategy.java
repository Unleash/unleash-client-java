package io.getunleash.strategy;

import static java.util.Arrays.asList;

import io.getunleash.UnleashContext;
import java.util.Map;
import java.util.Optional;

public final class UserWithIdStrategy implements Strategy {

    protected static final String PARAM = "userIds";
    private static final String STRATEGY_NAME = "userWithId";

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        return unleashContext
                .getUserId()
                .map(
                        currentUserId ->
                                Optional.ofNullable(parameters.get(PARAM))
                                        .map(userIdString -> asList(userIdString.split(",\\s?")))
                                        .filter(f -> f.contains(currentUserId))
                                        .isPresent())
                .orElse(false);
    }
}
