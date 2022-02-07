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

  @Override
  public boolean evaluate(Constraint constraint, UnleashContext context) {
    ZonedDateTime dateToMatch =
        context
            .getByName(constraint.getContextName())
            .map(DateParser::parseDate)
            .orElseGet(() -> context.getCurrentTime().orElseGet(ZonedDateTime::now));
    try {
      ZonedDateTime value = DateParser.parseDate(constraint.getValue());
      return eval(constraint.getOperator(), value, dateToMatch);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean eval(Operator op, ZonedDateTime value, ZonedDateTime toMatch) {
    switch (op) {
      case DATE_AFTER:
        return toMatch.isAfter(value);
      case DATE_BEFORE:
        return toMatch.isBefore(value);
      default:
        return false;
    }
  }
}
