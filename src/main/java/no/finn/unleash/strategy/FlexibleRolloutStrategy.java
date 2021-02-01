package no.finn.unleash.strategy;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import no.finn.unleash.UnleashContext;

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
            default:
                String value =
                        context.getUserId()
                                .orElse(context.getSessionId().orElse(this.randomGenerator.get()));
                return Optional.of(value);
        }
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext unleashContext) {
        final String stickiness = getStickiness(parameters);
        final Optional<String> stickinessId = resolveStickiness(stickiness, unleashContext);
        final int percentage = StrategyUtils.getPercentage(parameters.get(PERCENTAGE));
        final String groupId = Optional.ofNullable(parameters.get(GROUP_ID)).orElse("");

        if (stickinessId.isPresent()) {
            final int normalizedUserId =
                    StrategyUtils.getNormalizedNumber(stickinessId.get(), groupId);
            return percentage > 0 && normalizedUserId <= percentage;
        } else {
            return false;
        }
    }

    private String getStickiness(Map<String, String> parameters) {
        Optional<String> stickiness = Optional.ofNullable(parameters.get("stickiness"));
        return stickiness.orElse("default");
    }
}
