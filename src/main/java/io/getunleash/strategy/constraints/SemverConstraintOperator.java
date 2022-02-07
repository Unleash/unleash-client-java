package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemverConstraintOperator implements ConstraintOperator {
  private static final Logger LOGGER = LoggerFactory.getLogger(SemverConstraintOperator.class);

  @Override
  public boolean evaluate(Constraint constraint, UnleashContext context) {
    return context
        .getByName(constraint.getContextName())
        .map(
            contextValue -> {
              try {
                return SemanticVersion.parse(contextValue);
              } catch (SemanticVersion.InvalidVersionException invalidVersionException) {
                LOGGER.info(
                    "Couldn't parse version [{}] from context - This is dynamic on evaluation, might not be your fault",
                    contextValue);
                return null;
              }
            })
        .map(
            contextVersion -> {
              try {
                SemanticVersion value = SemanticVersion.parse(constraint.getValue());
                return eval(constraint.getOperator(), value, contextVersion);
              } catch (SemanticVersion.InvalidVersionException ive) {
                return null;
              }
            })
        .orElse(false);
  }

  private boolean eval(Operator operator, SemanticVersion value, SemanticVersion contextVersion) {
    switch (operator) {
      case SEMVER_LT:
        return contextVersion.compareTo(value) < 0;
      case SEMVER_EQ:
        return contextVersion.compareTo(value) == 0;
      case SEMVER_GT:
        return contextVersion.compareTo(value) > 0;
      default:
        return false;
    }
  }
}
