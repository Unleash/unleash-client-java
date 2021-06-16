package io.getunleash.strategy;

import static org.junit.jupiter.api.Assertions.*;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Test
    public void should_be_enabled_for_empty_constraints() {
        Strategy s = new AlwaysEnabled();
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().build();
        List<Constraint> constraints = new ArrayList<>();

        boolean result = s.isEnabled(parameters, context, constraints);
        assertTrue(result);
    }

    @Test
    public void should_be_enabled_for_null_constraints() {
        Strategy s = new AlwaysEnabled();
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().build();
        List<Constraint> constraints = null;

        boolean result = s.isEnabled(parameters, context, constraints);
        assertTrue(result);
    }

    @Test
    public void should_be_disabled_when_constraint_not_satisfied() {
        Strategy s = new AlwaysEnabled();
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().environment("test").build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("prod")));

        boolean result = s.isEnabled(parameters, context, constraints);
        assertFalse(result);
    }

    @Test
    public void should_be_enabled_when_constraint_is_satisfied() {
        Strategy s = new AlwaysEnabled();
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().environment("test").build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test", "prod")));

        boolean result = s.isEnabled(parameters, context, constraints);
        assertTrue(result);
    }

    @Test
    public void should_be_enabled_when_constraint_NOT_IN_satisfied() {
        Strategy s = new AlwaysEnabled();
        Map parameters = new HashMap<String, String>();
        UnleashContext context = UnleashContext.builder().environment("test").build();
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.NOT_IN, Arrays.asList("prod")));

        boolean result = s.isEnabled(parameters, context, constraints);
        assertTrue(result);
    }

    @Test
    public void should_be_enabled_when_all_constraints_are_satisfied() {
        Strategy s = new AlwaysEnabled();
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

        boolean result = s.isEnabled(parameters, context, constraints);
        assertTrue(result);
    }

    @Test
    public void should_be_disabled_when_not_all_constraints_are_satisfied() {
        Strategy s = new AlwaysEnabled();
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

        boolean result = s.isEnabled(parameters, context, constraints);
        assertFalse(result);
    }
}
