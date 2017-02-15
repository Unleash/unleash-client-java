package no.finn.unleash.strategy;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import no.finn.unleash.strategy.Strategy;

public final class GradualRolloutRandomStrategy implements Strategy {
    protected static final String PERCENTAGE = "percentage";
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
        int percentage = StrategyUtils.getPercentage(parameters.get(PERCENTAGE));
        int randomNumber = random.nextInt(100) + 1;
        return percentage >= randomNumber;
    }
}
