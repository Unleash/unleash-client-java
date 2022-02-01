package io.getunleash.strategy.constraints;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SemverConstraintOperator implements ConstraintOperator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SemverConstraintOperator.class);

    @Override
    public boolean evaluate(Constraint constraint, UnleashContext context) {
        return context.getByName(constraint.getContextName()).map(contextValue -> {
            try {
                return SemanticVersion.parse(contextValue);
            } catch (SemanticVersion.InvalidVersionException invalidVersionException) {
                LOGGER.info("Couldn't parse version [{}] from context - This is dynamic on evaluation, might not be your fault", contextValue);
                return null;
            }
        }).map(contextVersion -> {
            List<SemanticVersion> valueVersions = constraint.getValues().stream().map(v -> {
                try {
                    return SemanticVersion.parse(v);
                } catch (SemanticVersion.InvalidVersionException invalidVersionException) {
                    LOGGER.warn("Couldn't parse version [{}] from configured values - This is set when configuring the constraints for the toggle in the Admin GUI", v);
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            return eval(constraint.getOperator(), valueVersions, contextVersion);
        }).orElse(false);
    }

    private boolean eval(Operator operator, List<SemanticVersion> values, SemanticVersion contextVersion) {
        if (values.isEmpty()) {
            return false;
        }
        switch (operator) {
            case SEMVER_LT:
                return values.stream().allMatch(v -> contextVersion.compareTo(v) < 0);
            case SEMVER_EQ:
                return values.stream().allMatch(v -> contextVersion.compareTo(v) == 0);
            case SEMVER_GT:
                return values.stream().allMatch(v -> contextVersion.compareTo(v) > 0);
            default:
                return false;
        }
    }
}
