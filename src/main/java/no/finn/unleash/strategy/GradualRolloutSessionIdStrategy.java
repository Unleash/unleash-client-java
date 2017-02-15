package no.finn.unleash.strategy;

import java.util.Map;
import java.util.Optional;

import no.finn.unleash.UnleashContext;

/**
 * Implements a gradual roll-out strategy based on session id.
 *
 * Using this strategy you can target only users bound to a session and gradually expose your
 * feature to higher percentage of the logged in user.
 *
 * This strategy takes two parameters:
 *  - percentage :  a number between 0 and 100. The percentage you want to enable the feature for.
 *  - groupId :     a groupId used for rolling out the feature. By using the same groupId for different
 *                  toggles you can correlate the user experience across toggles.
 *
 */
public final class GradualRolloutSessionIdStrategy implements Strategy {
    protected static final String PERCENTAGE = "percentage";
    protected static final String GROUP_ID = "groupId";

    private static final String NAME = "gradualRolloutSessionId";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(final Map<String, String> parameters, UnleashContext unleashContext) {
        Optional<String> sessionId = unleashContext.getSessionId();

        if(!sessionId.isPresent()) {
            return false;
        }

        final int percentage = StrategyUtils.getPercentage(parameters.get(PERCENTAGE));
        final String groupId = Optional.ofNullable(parameters.get(GROUP_ID)).orElse("");

        final int normalizedSessionId = StrategyUtils.getNormalizedNumber(sessionId.get(), groupId);

        return percentage > 0 && normalizedSessionId <= percentage;
    }


}

