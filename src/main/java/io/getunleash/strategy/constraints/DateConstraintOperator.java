package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DateConstraintOperator implements ConstraintOperator {
    public static List<DateTimeFormatter> formatters = new ArrayList<>();

    static {
        formatters.add(DateTimeFormatter.ISO_INSTANT);
        formatters.add(DateTimeFormatter.ISO_DATE_TIME);
        formatters.add(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        formatters.add(DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    @Override
    public boolean evaluate(Constraint constraint, UnleashContext context) {
        ZonedDateTime dateToMatch = context.getByName(constraint.getContextName()).map(this::parseDate)
            .orElseGet(ZonedDateTime::now);
        List<ZonedDateTime> values = constraint.getValues().stream().map(value -> {
            try {
                return parseDate(value);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return eval(constraint.getOperator(), values, dateToMatch);
    }

    private boolean eval(Operator op, List<ZonedDateTime> values, ZonedDateTime toMatch) {
        switch (op) {
            case DATE_AFTER:
                return values.stream().anyMatch(toMatch::isAfter);
            case DATE_BEFORE:
                return values.stream().anyMatch(toMatch::isBefore);
            default:
                return false;
        }
    }

    private ZonedDateTime parseDate(String value) {
        return formatters.stream().map(f -> {
            try {
                return ZonedDateTime.parse(value, f);
            } catch (DateTimeParseException dateTimeParseException) {
                return null;
            }
        }).filter(Objects::nonNull).findFirst().orElseGet(() -> {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneOffset.UTC);
            } catch (DateTimeParseException dateTimeParseException) {
                return null;
            }
        });
    }
}
