package io.getunleash.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.getunleash.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getunleash.repository.UnleashEngineStateHandler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StrategyTest {

    private class AlwaysEnabled implements Strategy {
        @Override
        public String getName() {
            return "enabled";
        }

        @Override
        public boolean isEnabled(Map<String, String> parameters) {
            return true;
        }
    }

    private DefaultUnleash engine;
    private UnleashEngineStateHandler stateHandler;

    @BeforeEach
    void init() {
        UnleashContextProvider contextProvider = mock(UnleashContextProvider.class);
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        UnleashConfig config =
            new UnleashConfig.Builder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .environment("test")
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .unleashContextProvider(contextProvider)
                .build();


        engine = new DefaultUnleash(config, new AlwaysEnabled());
        stateHandler = new UnleashEngineStateHandler(engine);
    }

    @Test
    public void should_be_enabled_for_empty_constraints() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().build();
        List<Constraint> constraints = new ArrayList<>();
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertTrue(result);
    }

    @Test
    public void should_be_enabled_for_null_constraints() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().build();
        List<Constraint> constraints = null;

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertTrue(result);
    }

    @Test
    public void should_be_disabled_when_constraint_not_satisfied() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().environment("test").build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("prod")));

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertFalse(result);
    }

    @Test
    public void should_be_enabled_when_constraint_is_satisfied() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().environment("test").build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertTrue(result);
    }

    @Test
    public void should_be_enabled_when_constraint_NOT_IN_satisfied() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().environment("test").build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.NOT_IN, Arrays.asList("prod")));

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertTrue(result);
    }

    @Test
    public void should_be_enabled_when_all_constraints_are_satisfied() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context =
                UnleashContext.builder()
                        .environment("test")
                        .userId("123")
                        .addProperty("customerId", "blue")
                        .build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));
        constraints.add(new Constraint("userId", Operator.IN, Arrays.asList("123")));
        constraints.add(new Constraint("customerId", Operator.IN, Arrays.asList("red", "blue")));

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertTrue(result);
    }

    @Test
    public void should_be_disabled_when_not_all_constraints_are_satisfied() {
        Map parameters = new HashMap<String, String>();
        UnleashContext context =
                UnleashContext.builder()
                        .environment("test")
                        .userId("123")
                        .addProperty("customerId", "orange")
                        .build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));
        constraints.add(new Constraint("userId", Operator.IN, Arrays.asList("123")));
        constraints.add(new Constraint("customerId", Operator.IN, Arrays.asList("red", "blue")));

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("enabled", parameters, constraints, null, null))
        ));

        boolean result = engine.isEnabled("test", context);
        assertFalse(result);
    }
}
