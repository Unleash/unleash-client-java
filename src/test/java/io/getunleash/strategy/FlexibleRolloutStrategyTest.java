package io.getunleash.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableList;
import io.getunleash.*;
import io.getunleash.repository.UnleashEngineStateHandler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class FlexibleRolloutStrategyTest {

    private DefaultUnleash engine;
    private UnleashEngineStateHandler stateHandler;

    @BeforeEach
    void init() {
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
    public void should_always_be_false() {
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(
                                new ActivationStrategy("flexibleRollout", new HashMap<>()))));
        assertFalse(engine.isEnabled("test"));
    }

    @Test
    public void should_NOT_be_enabled_for_rollout_9_and_userId_61() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "9");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertFalse(engine.isEnabled("test", context));
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_userId_61() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_userId_61_and_stickiness_userId() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "userId");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().userId("61").build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_be_disabled_when_userId_missing() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "100");
        params.put("stickiness", "userId");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertFalse(engine.isEnabled("test", context));
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_sessionId_61() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().sessionId("61").build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_sessionId() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "sessionId");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().sessionId("61").build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    @Disabled // TODO we can't inject a random generator into the strategy it can be done providing
    // a custom strategy though in which case we should keep the strategy classes around
    // but it can't override the existing flexibleRollout
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_default() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    @Disabled // TODO we can't inject a random generator into the strategy it can be done providing
    // a custom strategy though in which case we should keep the strategy classes around
    // but it can't override the existing flexibleRollout
    public void should_be_enabled_for_rollout_10_and_randomId_61_and_stickiness_random() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "random");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_NOT_be_enabled_for_rollout_10_and_randomId_1() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "10");
        params.put("stickiness", "default");
        params.put("groupId", "Demo");

        UnleashContext context = UnleashContext.builder().build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertFalse(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_be_enabled_for_custom_field_402() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "50");
        params.put("stickiness", "customField");
        params.put("groupId", "Feature.flexible.rollout.custom.stickiness_50");
        UnleashContext context = UnleashContext.builder().addProperty("customField", "402").build();

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertFalse(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_be_enabled_for_custom_field_388_and_39() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "50");
        params.put("stickiness", "customField");
        params.put("groupId", "Feature.flexible.rollout.custom.stickiness_50");

        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));

        UnleashContext context = UnleashContext.builder().addProperty("customField", "388").build();
        assertTrue(engine.isEnabled("test", context));
        context = UnleashContext.builder().addProperty("customField", "39").build();
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_be_enabled_with_custom_stickiness_if_custom_field_is_missing() {
        Map<String, String> params = new HashMap<>();
        params.put("rollout", "50");
        params.put("stickiness", "customField");
        params.put("groupId", "Feature.flexible.rollout.custom.stickiness_50");
        UnleashContext context = UnleashContext.builder().build();
        stateHandler.setState(
                new FeatureToggle(
                        "test",
                        true,
                        ImmutableList.of(new ActivationStrategy("flexibleRollout", params))));
        assertFalse(engine.isEnabled("test", context));
    }
}
