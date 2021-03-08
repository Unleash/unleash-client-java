package no.finn.unleash.strategy;

import static java.util.Arrays.asList;
import static no.finn.unleash.strategy.GradualRolloutUserIdStrategy.GROUP_ID;

import java.util.HashMap;
import java.util.Map;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.lang.Nullable;

public class GradualContextMatchingStrategy implements Strategy {

    @Override
    public String getName() {
        return "gradualContextMatching";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        String groupId = parameters.getOrDefault(GROUP_ID, "");

        Map<String, String> combinedProperties = new HashMap<>(unleashContext.getProperties());
        unleashContext.getUserId().ifPresent(id -> combinedProperties.put("userId", id));
        unleashContext.getAppName().ifPresent(id -> combinedProperties.put("appName", id));
        unleashContext.getEnvironment().ifPresent(id -> combinedProperties.put("environment", id));

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (entry.getKey().equals(GROUP_ID)) {
                // Skip parameters that aren't intended for filtering
                continue;
            }

            if (entry.getKey().endsWith("::percentage")) {
                if (!percentageAllowed(
                        entry.getKey(), entry.getValue(), groupId, combinedProperties)) {
                    return false;
                }
            } else if (!contextMatchesParameter(
                    entry.getValue(), combinedProperties.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private boolean percentageAllowed(
            String keyWithSuffix,
            String value,
            String groupId,
            Map<String, String> combinedProperties) {
        String key = keyWithSuffix.replaceFirst("::percentage", "");
        String contextValue = combinedProperties.get(key);
        if (contextValue == null) {
            return false;
        }

        final int percentage = StrategyUtils.getPercentage(value);

        final int normalizedParameters = StrategyUtils.getNormalizedNumber(contextValue, groupId);

        return percentage > 0 && normalizedParameters <= percentage;
    }

    private boolean contextMatchesParameter(
            @Nullable String parameterValue, @Nullable String contextValue) {
        if (parameterValue != null && contextValue != null) {
            return asList(parameterValue.split(",")).contains(contextValue);
        }
        return false;
    }
}
