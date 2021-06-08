package io.getunleash.strategy;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Map;
import io.getunleash.UnleashContext;

public class StrategyUsingContext implements Strategy {

    @Override
    public String getName() {
        return "usingContext";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        String userIdString = parameters.get("userIds");
        List<String> userIds = asList(userIdString.split(",\\s?"));
        if (unleashContext.getUserId().isPresent()) {
            String userId = unleashContext.getUserId().get();
            return userIds.contains(userId);
        } else {
            return false;
        }
    }
}
