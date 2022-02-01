package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import io.getunleash.strategy.DefaultStrategy;
import io.getunleash.strategy.Strategy;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NumberConstraintOperatorTest {

    @Test
    public void shouldSupportEqForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_EQ, Collections.singletonList("42")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("userAge", "42")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isTrue();

    }

    @Test
    public void shouldSupportInvertingNumberEq() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_EQ, Collections.singletonList("42"), true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("userAge", "42")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isFalse();

    }

    @Test
    public void shouldSupportGreaterThanComparisonForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_GT, Collections.singletonList("42")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();

    }

    @Test
    public void shouldSupportInvertingGreaterThanComparisonForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_GT, Collections.singletonList("42"), true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
    }

    @Test
    public void shouldSupportLesserThanForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_LT, Collections.singletonList("42"), false));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();

    }

    @Test
    public void shouldSupportInvertingLesserThanForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_LT, Collections.singletonList("42"), true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();

    }

    @Test
    public void shouldSupportLessThanOrEqualForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_LTE, Collections.singletonList("42"), false));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        UnleashContext equal = UnleashContext.builder().environment("dev").addProperty("userAge", "42")
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isTrue();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();
    }

    @Test
    public void shouldSupportInvertingLessThanOrEqualForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_LTE, Collections.singletonList("42"), true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isFalse();
        UnleashContext equal = UnleashContext.builder().environment("dev").addProperty("userAge", "42")
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isFalse();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isTrue();

    }

    @Test
    public void shouldSupportGreaterThanOrEqualForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_GTE, Collections.singletonList("42"), false));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        UnleashContext equal = UnleashContext.builder().environment("dev").addProperty("userAge", "42")
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isTrue();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();

    }

    @Test
    public void shouldSupportInvertingGreaterThanOrEqualForNumbers() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("userAge", Operator.NUM_GTE, Collections.singletonList("42"), true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev").addProperty("userAge", "45")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isFalse();
        UnleashContext equal = UnleashContext.builder().environment("dev").addProperty("userAge", "42")
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isFalse();
        UnleashContext disabled = UnleashContext.builder().environment("dev").addProperty("userAge", "35")
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isTrue();

    }

}
