 package no.finn.unleash.metric;

 import no.finn.unleash.event.UnleashEvent;
 import no.finn.unleash.event.UnleashSubscriber;
 import no.finn.unleash.util.UnleashConfig;

 import java.time.LocalDateTime;
 import java.util.Set;

public class ClientRegistration implements UnleashEvent {
    private final String appName;
    private final String instanceId;
    private final String sdkVersion;
    private final Set<String> strategies;
    private final LocalDateTime started;
    private final long interval;
    private final String environment;


    ClientRegistration(UnleashConfig config, LocalDateTime started, Set<String> strategies) {
        this.environment = config.getEnvironment();
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

    public long getSendMetricsInterval() {
        return interval;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.clientRegistered(this);
    }

    @Override
    public String toString() {
        return "client registration:"
                + " appName=" + appName
                + " instanceId=" + instanceId
                + " sdkVersion=" + sdkVersion
                + " started=" + started
                + " interval=" + interval
                + " strategies=" + strategies
                + " environment=" + environment
                ;
    }
}
