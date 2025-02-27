package io.getunleash.metric;

import io.getunleash.engine.UnleashEngine;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import java.time.LocalDateTime;
import java.util.Set;

public class ClientRegistration implements UnleashEvent {
    private final String appName;
    private final String instanceId;
    private final String connectionId;
    private final String sdkVersion;
    private final Set<String> strategies;
    private final LocalDateTime started;
    private final long interval;
    private final String environment;
    @Nullable private final String platformName;
    @Nullable private final String platformVersion;
    @Nullable private final String yggdrasilVersion;
    private final String specVersion;

    ClientRegistration(UnleashConfig config, LocalDateTime started, Set<String> strategies) {
        this.environment = config.getEnvironment();
        this.appName = config.getAppName();
        this.instanceId = config.getInstanceId();
        this.sdkVersion = config.getSdkVersion();
        this.connectionId = config.getConnectionId();
        this.started = started;
        this.strategies = strategies;
        this.interval = config.getSendMetricsInterval();
        this.specVersion = config.getClientSpecificationVersion();
        this.platformName = System.getProperty("java.vm.name");
        this.platformVersion = System.getProperty("java.version");
        this.yggdrasilVersion = UnleashEngine.getCoreVersion();
    }

    public String getAppName() {
        return appName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getConnectionId() {
        return connectionId;
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

    public String getEnvironment() {
        return environment;
    }

    @Nullable
    public String getPlatformName() {
        return platformName;
    }

    @Nullable
    public String getPlatformVersion() {
        return platformVersion;
    }

    public @Nullable String getYggdrasilVersion() {
        return yggdrasilVersion;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.clientRegistered(this);
    }

    @Override
    public String toString() {
        return "client registration:"
                + " appName="
                + appName
                + " instanceId="
                + instanceId
                + " sdkVersion="
                + sdkVersion
                + " started="
                + sdkVersion
                + " interval="
                + sdkVersion
                + " strategies="
                + strategies;
    }
}
