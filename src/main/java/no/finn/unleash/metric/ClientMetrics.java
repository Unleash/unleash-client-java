package no.finn.unleash.metric;

import no.finn.unleash.util.UnleashConfig;

class ClientMetrics {

    private final String appName;
    private final String instanceId;
    private final MetricsBucket bucket;

    ClientMetrics(UnleashConfig config, MetricsBucket bucket) {
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
}
