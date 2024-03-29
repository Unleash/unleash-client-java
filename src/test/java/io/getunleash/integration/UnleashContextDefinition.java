package io.getunleash.integration;

import java.util.Map;

public class UnleashContextDefinition {
    private final String userId;
    private final String sessionId;
    private final String remoteAddress;
    private final String environment;
    private final String appName;
    private final String currentTime;

    // Custom context fields used in tests
    private final Map<String, String> properties;

    public UnleashContextDefinition(
            String userId,
            String sessionId,
            String remoteAddress,
            String environment,
            String appName,
            String currentTime,
            Map<String, String> properties) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.remoteAddress = remoteAddress;
        this.environment = environment;
        this.appName = appName;
        this.currentTime = currentTime;
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

    public String getEnvironment() {
        return environment;
    }

    public String getAppName() {
        return appName;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public Map<String, String> getProperties() {
        return properties;
    }
}
