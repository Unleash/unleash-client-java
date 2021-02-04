package no.finn.unleash.strategy;

import java.util.List;
import java.util.Optional;
import no.finn.unleash.Constraint;
import no.finn.unleash.Operator;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.lang.Nullable;

public class ConstraintUtil {

    public static boolean validate(@Nullable List<Constraint> constraints, UnleashContext context) {
        if (constraints != null && constraints.size() > 0) {
            return constraints.stream().allMatch(c -> validateConstraint(c, context));
        } else {
            return true;
        }
    }

    private static boolean validateConstraint(Constraint constraint, UnleashContext context) {
        Optional<String> contextValue = context.getByName(constraint.getContextName());
        boolean isIn =
                contextValue.isPresent()
                        && constraint.getValues().contains(contextValue.get().trim());
        return (constraint.getOperator() == Operator.IN) == isIn;
    }
}
