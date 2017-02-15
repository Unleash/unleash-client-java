package no.finn.unleash.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class GradualRolloutRandomStrategyTest {

    private static GradualRolloutRandomStrategy gradualRolloutRandomStrategy;

    @Before
    public void setUp() {
        long seed = new Random().nextLong();
        System.out.println("GradualRolloutRandomStrategyTest running with seed: " + seed);
        gradualRolloutRandomStrategy = new GradualRolloutRandomStrategy(seed);
    }

    @Test
    public void should_not_be_enabled_when_percentage_not_set() {
        final Map<String, String> parameters = new HashMap<>();

        final boolean enabled = gradualRolloutRandomStrategy.isEnabled(parameters);

        assertFalse(enabled);
    }

    @Test
    public void should_not_be_enabled_when_percentage_is_not_a_not_a_number() {
        final Map<String, String> parameters = new HashMap<String, String>() {{
            put("percentage", "foo");
        }};

        final boolean enabled = gradualRolloutRandomStrategy.isEnabled(parameters);

        assertFalse(enabled);
    }

    @Test
    public void should_not_be_enabled_when_percentage_is_not_a_not_a_valid_percentage_value() {
        final Map<String, String> parameters = new HashMap<String, String>() {{
            put("percentage", "ab");
        }};

        final boolean enabled = gradualRolloutRandomStrategy.isEnabled(parameters);

        assertFalse(enabled);
    }

    @Test
    public void should_never_be_enabled_when_0_percent() {
        final Map<String, String> parameters = new HashMap<String, String>() {{
            put("percentage", "0");
        }};

        for (int i = 0; i < 1000; i++) {
            final boolean enabled = gradualRolloutRandomStrategy.isEnabled(parameters);
            assertFalse(enabled);
        }

    }

    @Test
    public void should_always_be_enabled_when_100_percent() {
        final Map<String, String> parameters = new HashMap<String, String>() {{
            put("percentage", "100");
        }};

        for (int i = 0; i <= 100; i++) {
            final boolean enabled = gradualRolloutRandomStrategy.isEnabled(parameters);
            assertTrue("Should be enabled for p=" + i, enabled);
        }
    }

    @Test
    public void should_diverage_at_most_with_one_percent_point() {
        int percentage = 55;
        final int min= percentage - 1;
        final int max = percentage + 1;

        final Map<String, String> parameters = new HashMap<String, String>() {{
            put("percentage", ""+percentage);
        }};

        int rounds = 20000;
        int countEnabled = 0;

        for (int i = 0; i < rounds; i++) {
            final boolean enabled = gradualRolloutRandomStrategy.isEnabled(parameters);
            if(enabled) {
                countEnabled = countEnabled + 1;
            }
        }

        long measuredPercentage = Math.round(((double) countEnabled / rounds * 100));

        assertTrue(measuredPercentage >= min);
        assertTrue(measuredPercentage <= max);
    }
}