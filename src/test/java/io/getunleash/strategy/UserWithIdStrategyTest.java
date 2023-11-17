package io.getunleash.strategy;

import com.google.common.collect.ImmutableList;
import io.getunleash.ActivationStrategy;
import io.getunleash.DefaultUnleash;
import io.getunleash.FeatureToggle;
import io.getunleash.UnleashContext;
import io.getunleash.repository.UnleashEngineStateHandler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class UserWithIdStrategyTest {
    private DefaultUnleash engine;
    private UnleashEngineStateHandler stateHandler;

    @BeforeEach
    void setup() {
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
    public void should_match_one_userId() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put("userIds", "123");

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_match_first_userId_in_list() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put("userIds", "123, 122, 121");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_match_middle_userId_in_list() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put("userIds", "123, 122, 121");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_match_last_userId_in_list() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put("userIds", "123, 122, 121");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_match_subparts_of_ids() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("12").build();
        parameters.put("userIds", "123, 122, 121, 212");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertFalse(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_match_csv_without_space() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("123").build();
        parameters.put("userIds", "123,122,121");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_match_real_ids() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("298261117").build();
        parameters.put(
                "userIds",
                "160118738, 1823311338, 1422637466, 2125981185, 298261117, 1829486714, 463568019, 271166598");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertTrue(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_match_real_ids() {
        Map<String, String> parameters = new HashMap<>();

        UnleashContext context = UnleashContext.builder().userId("32667774").build();
        parameters.put(
                "userIds",
                "160118738, 1823311338, 1422637466, 2125981185, 298261117, 1829486714, 463568019, 271166598");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertFalse(engine.isEnabled("test", context));
    }

    @Test
    public void should_not_be_enabled_without_id() {
        Map<String, String> parameters = new HashMap<>();

        parameters.put("userIds", "160118738, 1823311338");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("userWithId", parameters))
        ));
        assertFalse(engine.isEnabled("test"));
    }
}
