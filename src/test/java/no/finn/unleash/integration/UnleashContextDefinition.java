package no.finn.unleash.integration;

import java.util.Map;

public class UnleashContextDefinition {
    private final String userId;
    private final String sessionId;
    private final String remoteAddress;
    private final Map<String, String> properties;

    public UnleashContextDefinition(String userId, String sessionId, String remoteAddress, Map<String, String> properties) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.properties = properties;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
