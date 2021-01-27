package no.finn.unleash.strategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import no.finn.unleash.UnleashContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GradualRolloutSessionIdStrategyTest {
    private static final long SEED = 89235015401L;
    private static final long MIN = 10000000L;
    private static final long MAX = 9999999999L;

    Random rand = new Random(SEED);
    List<Integer> percentages;

    @BeforeEach
    public void init() {
        percentages = ImmutableList.<Integer>builder()
                .add(1)
                .add(2)
                .add(5)
                .add(10)
                .add(25)
                .add(50)
                .add(90)
                .add(99)
                .add(100)
                .build();
    }

    @Test
    public void should_have_a_name() {
        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();
        assertThat(gradualRolloutStrategy.getName()).isEqualTo("gradualRolloutSessionId");
    }

    @Test
    public void should_require_context() {
        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();
        assertThat(gradualRolloutStrategy.isEnabled(new HashMap<>())).isFalse();
    }

    @Test
    public void should_be_disabled_when_missing_user_id() {
        UnleashContext context = UnleashContext.builder().build();
        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();

        assertThat(gradualRolloutStrategy.isEnabled(new HashMap<>(), context)).isFalse();
    }

    @Test
    public void should_have_same_result_for_multiple_executions() {
        UnleashContext context = UnleashContext.builder().sessionId("1574576830").build();
        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();

        Map<String, String> params = buildParams(1, "innfinn");
        boolean firstRunResult = gradualRolloutStrategy.isEnabled(params, context);

        for (int i = 0; i < 10; i++) {
            boolean subsequentRunResult = gradualRolloutStrategy.isEnabled(params, context);
            assertThat(firstRunResult).isEqualTo(subsequentRunResult).withFailMessage("loginId should return same result when unchanged parameters");
        }
    }

    @Test
    public void should_be_enabled_when_using_100percent_rollout() {
        UnleashContext context = UnleashContext.builder().sessionId("1574576830").build();
        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();

        Map<String, String> params = buildParams(100, "innfinn");
        boolean result = gradualRolloutStrategy.isEnabled(params, context);

        assertThat(result).isTrue();
    }


    @Test
    public void should_not_be_enabled_when_0percent_rollout() {
        UnleashContext context = UnleashContext.builder().sessionId("1574576830").build();
        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();

        Map<String, String> params = buildParams(0, "innfinn");
        boolean actual = gradualRolloutStrategy.isEnabled(params, context);

        assertFalse(actual, "should not be enabled when 0% rollout");
    }

    @Test
    public void should_be_enabled_above_minimum_percentage() {
        String sessionId = "1574576830";
        String groupId = "";
        int minimumPercentage = StrategyUtils.getNormalizedNumber(sessionId, groupId);

        UnleashContext context = UnleashContext.builder().sessionId(sessionId).build();

        GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();

        for(int p = minimumPercentage ; p <=100 ; p++) {
            Map<String, String> params = buildParams(p, groupId);
            boolean actual = gradualRolloutStrategy.isEnabled(params, context);
            assertTrue(actual, "should be enabled when " + p + "% rollout");
        }
    }

    @Disabled // Intended for manual execution
    @Test
    public void generateReportForListOfLoginIDs() {
        final int numberOfIDs = 200000;

        for (Integer percentage : percentages) {
            int numberOfEnabledUsers = checkRandomLoginIDs(numberOfIDs, percentage);
            double p = ((double) numberOfEnabledUsers / (double) numberOfIDs) * 100.0;
            System.out.println("Testing " + percentage + "% --> " + numberOfEnabledUsers + " of " + numberOfIDs + " got new feature (" + p + "%)");
        }
    }


    protected int checkRandomLoginIDs(int numberOfIDs, int percentage) {
        int numberOfEnabledUsers = 0;
        for (int i = 0; i < numberOfIDs; i++) {
            Long sessionId = getRandomLoginId();
            UnleashContext context = UnleashContext.builder().sessionId(sessionId.toString()).build();

            GradualRolloutSessionIdStrategy gradualRolloutStrategy = new GradualRolloutSessionIdStrategy();

            Map<String, String> params = buildParams(percentage, "");
            boolean enabled = gradualRolloutStrategy.isEnabled(params, context);
            if (enabled) {
                numberOfEnabledUsers++;
            }
        }
        return numberOfEnabledUsers;
    }

    private Map<String, String> buildParams(int percentage, String groupId) {
        Map<String, String> params = new HashMap();
        params.put(GradualRolloutSessionIdStrategy.PERCENTAGE, String.valueOf(percentage));
        params.put(GradualRolloutSessionIdStrategy.GROUP_ID, groupId);

        return params;
    }


    private Long getRandomLoginId() {
        long bits, val;
        long bound = (MAX - MIN) + 1L;
        do {
            bits = (rand.nextLong() << 1) >>> 1;
            val = bits % bound;
        } while (bits - val + (bound - 1L) < 0L);
        return val;
    }
}