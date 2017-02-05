package no.finn.unleash.strategy;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import no.finn.unleash.strategy.Strategy;

public final class GradualRolloutRandomStrategy implements Strategy {

    private static final String STRATEGY_NAME = "gradualRolloutRandom";

    private final Random random;

    public GradualRolloutRandomStrategy() {
        random = new Random();
    }

    protected GradualRolloutRandomStrategy(long seed) {
        random = new Random(seed);
    }

    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(final Map<String, String> parameters) {
        return Optional.ofNullable(parameters.get("percentage"))
                .filter(percentageStr -> percentageStr.matches("^100|0|[1-9][0-9]?$"))
                .map(Integer::parseInt)
                .map(percentage -> percentage >= random.nextInt(100) + 1)
                .orElse(false);
    }
}
