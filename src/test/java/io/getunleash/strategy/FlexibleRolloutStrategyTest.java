package io.getunleash.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import io.getunleash.UnleashContext;
import org.junit.jupiter.api.Test;

class FlexibleRolloutStrategyTest {

    @Test
    public void should_have_correct_name() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        assertEquals("flexibleRollout", strategy.getName());
    }

    @Test
    public void should_always_be_false() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        assertFalse(strategy.isEnabled(new HashMap<>()));
    }

    @Test
    public void should_NOT_be_enabled_for_rollout_9_and_userId_61() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "9");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertFalse(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_userId_61() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_userId_61_and_stickiness_userId() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "userId");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_disabled_when_userId_missing() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "100");
        params.put("stickiness", "userId");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertFalse(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_sessionId_61() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().sessionId("61").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_sessionId() {
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "sessionId");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().sessionId("61").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_default() {
        Supplier<String> radnomGenerator = () -> "61";
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(radnomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_random() {
        Supplier<String> radnomGenerator = () -> "61";
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(radnomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "random");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertTrue(enabled);
    }

    @Test
    public void should_NOT_be_enabled_for_rollout_10_and_randomId_1() {
        Supplier<String> radnomGenerator = () -> "1";
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(radnomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertFalse(enabled);
    }

    @Test
    public void should_not_be_enabled_for_custom_field_402() {
        Supplier<String> randomGenerator = () -> "2";
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(randomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "50");
        params.put("stickiness", "customField");
        params.put("groupId", "Feature.flexible.rollout.custom.stickiness_50");
        UnleashContext context = UnleashContext.builder().addProperty("customField", "402").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertThat(enabled).isFalse();
    }

    @Test
    public void should_not_be_enabled_for_custom_field_388_and_39() {
        Supplier<String> randomGenerator = () -> "2";
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(randomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "50");
        params.put("stickiness", "customField");
        params.put("groupId", "Feature.flexible.rollout.custom.stickiness_50");
        UnleashContext context = UnleashContext.builder().addProperty("customField", "388").build();
        boolean enabled = strategy.isEnabled(params, context);
        assertThat(enabled).isTrue();
        context = UnleashContext.builder().addProperty("customField", "39").build();
        enabled = strategy.isEnabled(params, context);
        assertThat(enabled).isTrue();
    }

    @Test
    public void should_not_be_enabled_with_custom_stickiness_if_custom_field_is_missing() {
        Supplier<String> randomGenerator = () -> "2";
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy(randomGenerator);
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "50");
        params.put("stickiness", "customField");
        params.put("groupId", "Feature.flexible.rollout.custom.stickiness_50");
        UnleashContext context = UnleashContext.builder().build();
        boolean enabled = strategy.isEnabled(params, context);
        assertThat(enabled).isFalse();
    }
}
