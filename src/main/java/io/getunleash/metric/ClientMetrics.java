package io.getunleash.metric;

import io.getunleash.engine.MetricsBucket;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.util.UnleashConfig;

public class ClientMetrics implements UnleashEvent {

    private final String appName;
    private final String instanceId;
    private final MetricsBucket bucket;
    private final String environment;

    ClientMetrics(UnleashConfig config, MetricsBucket bucket) {
        this.environment = config.getEnvironment();
        this.appName = config.getAppName();
        this.instanceId = config.getInstanceId();
        this.bucket = bucket;
    }

    public String getAppName() {
        return appName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public MetricsBucket getBucket() {
        return bucket;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.clientMetrics(this);
    }

    @Override
    public String toString() {
        return "metrics:" + " appName=" + appName + " instanceId=" + instanceId;
    }
}
