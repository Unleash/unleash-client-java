package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import io.getunleash.strategy.DefaultStrategy;
import io.getunleash.strategy.Strategy;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DateConstraintOperatorTest {
    DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;
    @Test
    public void shouldSupportAfterDate() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("releaseDate", Operator.DATE_AFTER, Collections.singletonList(ZonedDateTime.now().minusHours(1).format(ISO))));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext enabled = UnleashContext.builder().environment("dev").build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isTrue();
        // If there's a date in the defined context field, compare with that
        UnleashContext equal = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isTrue();

        UnleashContext disabled = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().minusHours(2).format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isFalse();

    }

    @Test
    public void afterDateSupportsInversion() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("releaseDate", Operator.DATE_AFTER, Collections.singletonList(ZonedDateTime.now().minusHours(1).format(ISO)), true));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext enabled = UnleashContext.builder().environment("dev").build();
        assertThat(strategy.isEnabled(parameters, enabled, constraintList)).isFalse();
        // If there's a date in the defined context field, compare with that
        UnleashContext equal = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isFalse();

        UnleashContext disabled = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().minusHours(2).format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, disabled, constraintList)).isTrue();

    }

    @Test
    public void shouldSupportBeforeDate() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("releaseDate", Operator.DATE_BEFORE, Collections.singletonList(ZonedDateTime.now()
            .plusDays(2).format(ISO))));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext now = UnleashContext.builder().environment("dev").build();
        assertThat(strategy.isEnabled(parameters, now, constraintList)).isTrue();
        // If there's a date in the defined context field, compare with that
        UnleashContext equal = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().plusDays(1).format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isTrue();

        UnleashContext fourDaysFromNow = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().plusDays(4).format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, fourDaysFromNow, constraintList)).isFalse();

    }

    @Test
    public void beforeDateShouldSupportInversion() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("releaseDate", Operator.DATE_BEFORE, Collections.singletonList(ZonedDateTime.now()
            .plusDays(2).format(ISO)), true, false));
        Map<String, String> parameters = new HashMap<>();
        // If there's not a date in the defined context field, compare with Instant.now()
        UnleashContext now = UnleashContext.builder().environment("dev").build();
        assertThat(strategy.isEnabled(parameters, now, constraintList)).isFalse();
        // If there's a date in the defined context field, compare with that
        UnleashContext equal = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().plusDays(1).format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isFalse();

        UnleashContext fourDaysFromNow = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", ZonedDateTime.now().plusDays(4).format(ISO))
            .build();
        assertThat(strategy.isEnabled(parameters, fourDaysFromNow, constraintList)).isTrue();

    }

    @Test
    public void dateParserSupportsNotPassingInZone() {
        Strategy strategy = new DefaultStrategy();
        List<Constraint> constraintList = Collections.singletonList(new Constraint("releaseDate", Operator.DATE_BEFORE, Collections.singletonList("2022-11-05T11:05:00")));
        Map<String, String> parameters = new HashMap<>();
        UnleashContext equal = UnleashContext.builder().environment("dev")
            .addProperty("releaseDate", "2022-11-04T11:05:00")
            .build();
        assertThat(strategy.isEnabled(parameters, equal, constraintList)).isTrue();
    }
}
