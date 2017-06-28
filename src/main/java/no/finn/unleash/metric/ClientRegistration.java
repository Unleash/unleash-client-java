 package no.finn.unleash.metric;

 import no.finn.unleash.util.UnleashConfig;

 import java.time.LocalDateTime;
 import java.util.Set;

class ClientRegistration {
    private final String appName;
    private final String instanceId;
    private final String sdkVersion;
    private final Set<String> strategies;
    private final LocalDateTime started;
    private final long interval;

    ClientRegistration(UnleashConfig config, LocalDateTime started, Set<String> strategies) {
        this.appName = config.getAppName();
        this.instanceId = config.getInstanceId();
        this.sdkVersion = config.getSdkVersion();
        this.started = started;
        this.strategies = strategies;
        this.interval = config.getSendMetricsInterval();
    }

    public String getAppName() {
        return appName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public Set<String> getStrategies() {
        return strategies;
    }

    public LocalDateTime getStarted() {
        return started;
    }

    public long getInterval() {
        return interval;
    }
}
