package no.finn.unleash;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UnleashContext {
    private final Optional<String> userId;
    private final Optional<String> sessionId;
    private final Optional<String> remoteAddress;

    private final Map<String, String> properties;

    public UnleashContext(String userId, String sessionId, String remoteAddress, Map<String, String> properties) {
        this.userId = Optional.ofNullable(userId);
        this.sessionId = Optional.ofNullable(sessionId);
        this.remoteAddress = Optional.ofNullable(remoteAddress);
        this.properties = properties;
    }

    public Optional<String> getUserId() {
        return userId;
    }

    public Optional<String> getSessionId() {
        return sessionId;
    }

    public Optional<String> getRemoteAddress() {
        return remoteAddress;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String sessionId;
        private String remoteAddress;

        private final Map<String, String> properties = new HashMap<>();

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.userId = sessionId;
            return this;
        }

        public Builder remoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder addProperty(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public UnleashContext build() {
            return new UnleashContext(userId, sessionId, remoteAddress, properties);
        }
    }


}
