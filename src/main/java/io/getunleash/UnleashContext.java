package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UnleashContext {
    private final Optional<String> appName;
    private final Optional<String> environment;
    private final Optional<String> userId;
    private final Optional<String> sessionId;
    private final Optional<String> remoteAddress;
    private final Optional<ZonedDateTime> currentTime;

    private final Map<String, String> properties;

    public UnleashContext(
            String userId, String sessionId, String remoteAddress, Map<String, String> properties) {
        this(null, null, userId, sessionId, remoteAddress, null, properties);
    }

    public UnleashContext(
            @Nullable String appName,
            @Nullable String environment,
            @Nullable String userId,
            @Nullable String sessionId,
            @Nullable String remoteAddress,
            Map<String, String> properties) {
        this(appName, environment, userId, sessionId, remoteAddress, null, properties);
    }

  public UnleashContext(
      @Nullable String appName,
      @Nullable String environment,
      @Nullable String userId,
      @Nullable String sessionId,
      @Nullable String remoteAddress,
      @Nullable ZonedDateTime currentTime,
      Map<String, String> properties) {
    this.appName = Optional.ofNullable(appName);
    this.environment = Optional.ofNullable(environment);
    this.userId = Optional.ofNullable(userId);
    this.sessionId = Optional.ofNullable(sessionId);
    this.remoteAddress = Optional.ofNullable(remoteAddress);
    this.currentTime = Optional.ofNullable(currentTime);
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

    public Optional<String> getAppName() {
        return appName;
    }

    public Optional<String> getEnvironment() {
        return environment;
    }

    public Optional<ZonedDateTime> getCurrentTime() {
        return currentTime;
    }

    public Optional<String> getByName(String contextName) {
        switch (contextName) {
            case "environment":
                return environment;
            case "appName":
                return appName;
            case "userId":
                return userId;
            case "sessionId":
                return sessionId;
            case "remoteAddress":
                return remoteAddress;
            default:
                return Optional.ofNullable(properties.get(contextName));
        }
    }

    public UnleashContext applyStaticFields(UnleashConfig config) {
        Builder builder = new Builder(this);
        if (!this.environment.isPresent()) {
            builder.environment(config.getEnvironment());
        }
        if (!this.appName.isPresent()) {
            builder.appName(config.getAppName());
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable private String appName;
        @Nullable private String environment;
        @Nullable private String userId;
        @Nullable private String sessionId;
        @Nullable private String remoteAddress;
        @Nullable private ZonedDateTime currentTime;

        private final Map<String, String> properties = new HashMap<>();

        public Builder() {}

        public Builder(UnleashContext context) {
            context.appName.ifPresent(val -> this.appName = val);
            context.environment.ifPresent(val -> this.environment = val);
            context.userId.ifPresent(val -> this.userId = val);
            context.sessionId.ifPresent(val -> this.sessionId = val);
            context.remoteAddress.ifPresent(val -> this.remoteAddress = val);
            context.currentTime.ifPresent(val -> this.currentTime = val);
            context.properties.forEach(this.properties::put);
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder remoteAddress(String remoteAddress) {
            this.remoteAddress = remoteAddress;
            return this;
        }

        public Builder currentTime(ZonedDateTime currentTime) {
            this.currentTime = currentTime;
            return this;
        }

        public Builder now() {
            this.currentTime = ZonedDateTime.now();
            return this;
        }

        public Builder addProperty(String name, String value) {
            properties.put(name, value);
            return this;
        }

        public UnleashContext build() {
            return new UnleashContext(
                    appName, environment, userId, sessionId, remoteAddress, currentTime, properties);
        }
    }
}
