package no.finn.unleash;

import java.util.Map;

/**
 * @deprecated use {@link io.getunleash.UnleashContext}
 */
@Deprecated
public class UnleashContext extends io.getunleash.UnleashContext {
    public UnleashContext(String userId, String sessionId, String remoteAddress, Map<String, String> properties) {
        super(userId, sessionId, remoteAddress, properties);
    }

    public UnleashContext(String appName, String environment, String userId, String sessionId, String remoteAddress, Map<String, String> properties) {
        super(appName, environment, userId, sessionId, remoteAddress, properties);
    }
}
