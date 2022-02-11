package io.getunleash.strategy;

import static io.getunleash.Operator.DATE_AFTER;
import static io.getunleash.Operator.DATE_BEFORE;
import static io.getunleash.Operator.IN;
import static io.getunleash.Operator.NOT_IN;
import static io.getunleash.Operator.NUM_EQ;
import static io.getunleash.Operator.NUM_GT;
import static io.getunleash.Operator.NUM_GTE;
import static io.getunleash.Operator.NUM_LT;
import static io.getunleash.Operator.NUM_LTE;
import static io.getunleash.Operator.SEMVER_EQ;
import static io.getunleash.Operator.SEMVER_GT;
import static io.getunleash.Operator.SEMVER_LT;
import static io.getunleash.Operator.STR_CONTAINS;
import static io.getunleash.Operator.STR_ENDS_WITH;
import static io.getunleash.Operator.STR_STARTS_WITH;

import io.getunleash.Constraint;
import io.getunleash.Operator;
import io.getunleash.UnleashContext;
import io.getunleash.lang.Nullable;
import io.getunleash.strategy.constraints.ConstraintOperator;
import io.getunleash.strategy.constraints.DateConstraintOperator;
import io.getunleash.strategy.constraints.NumberConstraintOperator;
import io.getunleash.strategy.constraints.SemverConstraintOperator;
import io.getunleash.strategy.constraints.StringConstraintOperator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ConstraintUtil {
    private static final Map<Operator, ConstraintOperator> operators = new HashMap<>();

    static {
        operators.put(STR_CONTAINS, new StringConstraintOperator(Locale.ROOT));
        operators.put(STR_ENDS_WITH, new StringConstraintOperator(Locale.ROOT));
        operators.put(STR_STARTS_WITH, new StringConstraintOperator(Locale.ROOT));
        operators.put(IN, new StringConstraintOperator(Locale.ROOT));
        operators.put(NOT_IN, new StringConstraintOperator(Locale.ROOT));
        operators.put(NUM_LT, new NumberConstraintOperator());
        operators.put(NUM_LTE, new NumberConstraintOperator());
        operators.put(NUM_EQ, new NumberConstraintOperator());
        operators.put(NUM_GTE, new NumberConstraintOperator());
        operators.put(NUM_GT, new NumberConstraintOperator());
        operators.put(SEMVER_LT, new SemverConstraintOperator());
        operators.put(SEMVER_EQ, new SemverConstraintOperator());
        operators.put(SEMVER_GT, new SemverConstraintOperator());
        operators.put(DATE_BEFORE, new DateConstraintOperator());
        operators.put(DATE_AFTER, new DateConstraintOperator());
    }

    public static boolean validate(@Nullable List<Constraint> constraints, UnleashContext context) {
        if (constraints != null && constraints.size() > 0) {
            return constraints.stream().allMatch(c -> validateConstraint(c, context));
        } else {
            return true;
        }
    }

    private static boolean validateConstraint(Constraint constraint, UnleashContext context) {
        ConstraintOperator operator = operators.get(constraint.getOperator());
        return constraint.isInverted() ^ operator.evaluate(constraint, context);
    }
}
