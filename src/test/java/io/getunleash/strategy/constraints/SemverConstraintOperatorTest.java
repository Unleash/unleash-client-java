package io.getunleash.strategy.constraints;

import static org.assertj.core.api.Assertions.assertThat;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import io.getunleash.strategy.DefaultStrategy;
import io.getunleash.strategy.Strategy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SemverConstraintOperatorTest {
    @Test
    public void shouldSupportSemverEq() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Collections.singletonList(
                        new Constraint(
                                "version", Operator.SEMVER_EQ, Collections.singletonList("1.2.4")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext correctVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, correctVersion, constraintList)).isTrue();
        UnleashContext wrongVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, wrongVersion, constraintList)).isFalse();
    }

    @Test
    public void shouldSupportInvertingSemverEq() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Collections.singletonList(
                        new Constraint(
                                "version",
                                Operator.SEMVER_EQ,
                                Collections.singletonList("1.2.4"),
                                true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext correctVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, correctVersion, constraintList)).isFalse();
        UnleashContext wrongVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, wrongVersion, constraintList)).isTrue();
    }

    @Test
    public void shouldSupportSemverLt() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Collections.singletonList(
                        new Constraint(
                                "version", Operator.SEMVER_LT, Collections.singletonList("2.0.0")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext correctVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, correctVersion, constraintList)).isTrue();
        UnleashContext wrongVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, wrongVersion, constraintList)).isFalse();
    }

    @Test
    public void shouldSupportInvertingSemverLt() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Collections.singletonList(
                        new Constraint(
                                "version",
                                Operator.SEMVER_LT,
                                Collections.singletonList("2.0.0"),
                                true));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext correctVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, correctVersion, constraintList)).isFalse();
        UnleashContext wrongVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, wrongVersion, constraintList)).isTrue();
    }

    @Test
    public void shouldSupportSemverGt() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Collections.singletonList(
                        new Constraint(
                                "version", Operator.SEMVER_GT, Collections.singletonList("2.0.0")));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext newerVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, newerVersion, constraintList)).isTrue();
        UnleashContext olderVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, olderVersion, constraintList)).isFalse();
    }

    @Test
    public void shouldSupportInvertingSemverGt() {

        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Collections.singletonList(
                        new Constraint(
                                "version",
                                Operator.SEMVER_GT,
                                Collections.singletonList("2.0.0"),
                                true));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext newerVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, newerVersion, constraintList)).isFalse();
        UnleashContext olderVersion =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, olderVersion, constraintList)).isTrue();
    }

    @Test
    public void shouldSupportSemverInRange() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList =
                Arrays.asList(
                        new Constraint(
                                "version", Operator.SEMVER_GT, Collections.singletonList("2.0.0")),
                        new Constraint(
                                "version", Operator.SEMVER_LT, Collections.singletonList("3.0.0")));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext inRange =
                UnleashContext.builder().environment("dev").addProperty("version", "2.2.4").build();
        assertThat(strategy.isEnabled(parameters, inRange, constraintList)).isTrue();
        UnleashContext lessThanRange =
                UnleashContext.builder().environment("dev").addProperty("version", "1.2.4").build();
        assertThat(strategy.isEnabled(parameters, lessThanRange, constraintList)).isFalse();
        UnleashContext moreThanRange =
                UnleashContext.builder().environment("dev").addProperty("version", "3.2.4").build();
        assertThat(strategy.isEnabled(parameters, moreThanRange, constraintList)).isFalse();
    }
}
