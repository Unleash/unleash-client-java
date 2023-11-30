package io.getunleash.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import io.getunleash.*;
import io.getunleash.repository.UnleashEngineStateHandler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class GradualRolloutRandomStrategyTest {
    private DefaultUnleash engine;
    private UnleashEngineStateHandler stateHandler;

    @BeforeEach
    void setUp() {
        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .environment("test")
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .build();

        engine = new DefaultUnleash(config);
        stateHandler = new UnleashEngineStateHandler(engine);
    }

    @Test
    public void should_not_be_enabled_when_percentage_not_set() {
        final Map<String, String> parameters = new HashMap<>();

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("gradualRolloutRandom", parameters))));
        final boolean enabled = engine.isEnabled("test");

        assertFalse(enabled);
    }

    @Test
    public void should_not_be_enabled_when_percentage_is_not_a_not_a_number() {
        final Map<String, String> parameters =
                new HashMap<String, String>() {
                    {
                        put("percentage", "foo");
                    }
                };

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("gradualRolloutRandom", parameters)),
                        Collections.emptyList()));
        final boolean enabled = engine.isEnabled("test");

        assertFalse(enabled);
    }

    @Test
    public void should_not_be_enabled_when_percentage_is_not_a_not_a_valid_percentage_value() {
        final Map<String, String> parameters =
                new HashMap<String, String>() {
                    {
                        put("percentage", "ab");
                    }
                };

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("gradualRolloutRandom", parameters)),
                        Collections.emptyList()));
        final boolean enabled = engine.isEnabled("test");

        assertFalse(enabled);
    }

    @Test
    public void should_never_be_enabled_when_0_percent() {
        final Map<String, String> parameters =
                new HashMap<String, String>() {
                    {
                        put("percentage", "0");
                    }
                };

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("gradualRolloutRandom", parameters)),
                        Collections.emptyList()));

        for (int i = 0; i < 1000; i++) {
            final boolean enabled = engine.isEnabled("test");
            assertFalse(enabled);
        }
    }

    @Test
    public void should_always_be_enabled_when_100_percent() {
        final Map<String, String> parameters =
                new HashMap<String, String>() {
                    {
                        put("percentage", "100");
                    }
                };

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("gradualRolloutRandom", parameters)),
                        Collections.emptyList()));
        for (int i = 0; i <= 100; i++) {
            final boolean enabled = engine.isEnabled("test");
            assertTrue(enabled, "Should be enabled for p=" + i);
        }
    }

    @Test
    public void should_diverage_at_most_with_one_percent_point() {
        int percentage = 55;
        final int min = percentage - 1;
        final int max = percentage + 1;

        final Map<String, String> parameters =
                new HashMap<String, String>() {
                    {
                        put("percentage", "" + percentage);
                    }
                };

        int rounds = 20000;
        int countEnabled = 0;

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("gradualRolloutRandom", parameters)),
                        Collections.emptyList()));
        for (int i = 0; i < rounds; i++) {
            final boolean enabled = engine.isEnabled("test");
            if (enabled) {
                countEnabled = countEnabled + 1;
            }
        }

        long measuredPercentage = Math.round(((double) countEnabled / rounds * 100));

        assertTrue(measuredPercentage >= min);
        assertTrue(measuredPercentage <= max);
    }
}
