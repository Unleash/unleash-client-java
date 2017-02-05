package no.finn.unleash.strategy;

import java.util.List;
import java.util.Map;
import no.finn.unleash.UnleashContext;

import static java.util.Arrays.asList;

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
        if(unleashContext.getUserId().isPresent()) {
            String userId = unleashContext.getUserId().get();
            return userIds.contains(userId);
        } else {
            return false;
        }
    }
}
