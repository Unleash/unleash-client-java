package io.getunleash.strategy;

import io.getunleash.UnleashContext;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class FlexibleRolloutStrategy implements Strategy {
    protected static final String PERCENTAGE = "rollout";
    protected static final String GROUP_ID = "groupId";

    private Supplier<String> randomGenerator;

    public FlexibleRolloutStrategy() {
        this.randomGenerator = () -> Math.random() * 100 + "";
    }

    public FlexibleRolloutStrategy(Supplier<String> randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    public String getName() {
        return "flexibleRollout";
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    private Optional<String> resolveStickiness(String stickiness, UnleashContext context) {
        switch (stickiness) {
            case "userId":
                return context.getUserId();
            case "sessionId":
                return context.getSessionId();
            case "random":
                return Optional.of(randomGenerator.get());
            case "default":
                String value =
                        context.getUserId()
                                .orElse(context.getSessionId().orElse(this.randomGenerator.get()));
                return Optional.of(value);
            default:
                return context.getByName(stickiness);
        }
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        final String stickiness = getStickiness(parameters);
        final Optional<String> stickinessId = resolveStickiness(stickiness, unleashContext);
        final int percentage = StrategyUtils.getPercentage(parameters.get(PERCENTAGE));
        final String groupId = parameters.getOrDefault(GROUP_ID, "");

        return stickinessId
                .map(stick -> StrategyUtils.getNormalizedNumber(stick, groupId))
                .map(norm -> percentage > 0 && norm <= percentage)
                .orElse(false);
    }

    private String getStickiness(Map<String, String> parameters) {
        return parameters.getOrDefault("stickiness", "default");
    }
}
