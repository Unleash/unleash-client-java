package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemverConstraintOperator implements ConstraintOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SemverConstraintOperator.class);

    @Override
    public boolean evaluate(Constraint constraint, UnleashContext context) {
        return context.getByName(constraint.getContextName())
                .map(
                        contextValue -> {
                            try {
                                return SemanticVersion.parse(contextValue);
                            } catch (
                                    SemanticVersion.InvalidVersionException
                                            invalidVersionException) {
                                LOGGER.info(
                                        "Couldn't parse version [{}] from context - This is dynamic on evaluation, might not be your fault",
                                        contextValue);
                                return null;
                            }
                        })
                .map(
                        contextVersion -> {
                            try {
                                if (constraint.getValues().size() > 0) {
                                    return constraint.getValues().stream()
                                            .map(
                                                    v -> {
                                                        try {
                                                            return SemanticVersion.parse(v);
                                                        } catch (
                                                                SemanticVersion
                                                                                .InvalidVersionException
                                                                        e) {
                                                            return null;
                                                        }
                                                    })
                                            .filter(Objects::nonNull)
                                            .anyMatch(
                                                    v ->
                                                            eval(
                                                                    constraint.getOperator(),
                                                                    v,
                                                                    contextVersion));
                                } else if (constraint.getValue() != null
                                        && constraint.getValue().length() > 0) {
                                    SemanticVersion value =
                                            SemanticVersion.parse(constraint.getValue());
                                    return eval(constraint.getOperator(), value, contextVersion);
                                } else {
                                    return null;
                                }
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
