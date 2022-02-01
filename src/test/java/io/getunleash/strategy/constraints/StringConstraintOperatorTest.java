package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import io.getunleash.strategy.DefaultStrategy;
import io.getunleash.strategy.Strategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StringConstraintOperatorTest {
    @Test
    public void shouldBeEnabledWhenEmailStartsWith() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_STARTS_WITH, Collections.singletonList("example")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@getunleash.ai")
            .build();
        assertThat(strategy.isEnabled(new HashMap<>(), ctx, constraintList)).isTrue();
    }

    @Test
    public void shouldBeEnabledWhenEmailStartsWithMultiple() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_STARTS_WITH, Arrays.asList("other", "example")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@getunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isTrue();
    }

    @Test
    public void shouldBeEnabledWhenEmailEndsWith() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_ENDS_WITH, Collections.singletonList("@getunleash.ai")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@getunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isTrue();

    }

    @Test
    public void shouldBeEnabledWhenEmailEndsWithIgnoringCase() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_ENDS_WITH, Collections.singletonList("@getunleash.ai"), false, true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@GETunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isTrue();

    }

    @Test
    public void shouldNotBeEnabledWhenEmailEndsWithCaseSensitive() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_ENDS_WITH, Collections.singletonList("@getunleash.ai")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@GETunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isFalse();

    }

    // Testing inversion of evaluation result
    @Test
    public void shouldBeEnabledWhenEmailDoesNotEndWith() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email",
            Operator.STR_ENDS_WITH,
            Collections.singletonList("@getunleash.ai"),
            true,
            false));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@something.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isTrue();

    }

    @Test
    public void shouldBeEnabledWhenEmailEndsWithMultipleEmails() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_ENDS_WITH, Arrays.asList("@getunleash.ai", "somerandom-email.com")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext enabled = UnleashContext.builder().environment("dev")
            .addProperty("email", "example@getunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        UnleashContext enabled2 = UnleashContext.builder().environment("dev")
            .addProperty("email", "example@somerandom-email.com")
            .build();
        assertThat(strategy.isEnabled(parameters, enabled2, constraintList)).isTrue();
        UnleashContext disabled = UnleashContext.builder().environment("dev")
            .addProperty("email", "example@some-email.com")
            .build();

        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();

    }

    @Test
    public void shouldNotBeDisabledWhenEmailDoesNotEndWith() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_ENDS_WITH, Collections.singletonList("@getunleash.ai")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@something-else")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isFalse();

    }

    @Test
    public void shouldBeEnabledWhenEmailContains() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_CONTAINS, Collections.singletonList("some")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev")
            .addProperty("email", "example-some@getunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isTrue();
    }

    @Test
    public void shouldSupportInvertingStringContains() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("email", Operator.STR_CONTAINS, Collections.singletonList("ample"), true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext ctx = UnleashContext.builder().environment("dev").addProperty("email", "example@GETunleash.ai")
            .build();
        assertThat(strategy.isEnabled(parameters, ctx, constraintList)).isFalse();
    }
}
