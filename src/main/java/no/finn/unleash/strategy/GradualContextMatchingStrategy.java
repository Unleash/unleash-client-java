package no.finn.unleash.strategy;

import static java.util.Arrays.asList;
import static no.finn.unleash.strategy.GradualRolloutUserIdStrategy.GROUP_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.lang.Nullable;

public class GradualContextMatchingStrategy implements Strategy {
    protected static final String PERCENTAGE = "percentage";

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
        String percentageParam = parameters.getOrDefault(PERCENTAGE, parameters.get("rollout"));

        Map<String, String> combinedProperties = new HashMap<>(unleashContext.getProperties());
        unleashContext.getUserId().ifPresent(id -> combinedProperties.put("userId", id));
        unleashContext.getAppName().ifPresent(id -> combinedProperties.put("appName", id));
        unleashContext.getEnvironment().ifPresent(id -> combinedProperties.put("environment", id));

        String combinedMatchingParameters =
                parameters.entrySet().stream()
                        .map(
                                entry -> {
                                    // Ignore percentage/groupId since those control gradual rollout
                                    if (entry.getKey().equals(PERCENTAGE)
                                            || entry.getKey().equals("rollout")
                                            || entry.getKey().equals(GROUP_ID)) {
                                        return "";
                                    }
                                    return parameterEnabledValue(
                                            parameters, entry.getKey(), entry.getValue());
                                })
                        .collect(Collectors.joining());

        if (combinedMatchingParameters.isEmpty()) {
            return false;
        } else if (percentageParam == null) {
            return true;
        }

        final int percentage = StrategyUtils.getPercentage(percentageParam);
        String groupId = parameters.getOrDefault(GROUP_ID, "");

        final int normalizedParameters =
                StrategyUtils.getNormalizedNumber(combinedMatchingParameters, groupId);

        return percentage > 0 && normalizedParameters <= percentage;
    }

    private String parameterEnabledValue(
            Map<String, String> parameters, String key, @Nullable String contextValue) {
        String parameterValue = parameters.getOrDefault(key, parameters.get(key + "s"));
        if (parameterValue != null
                && contextValue != null
                && asList(parameterValue.split(",")).contains(contextValue)) {
            return contextValue;
        }
        return "";
    }
}
