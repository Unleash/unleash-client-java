package no.finn.unleash.util;

import java.net.URI;

public class UnleashConfig {
    private final URI unleashAPI;
    private final UnleashURLs unleashURLs;
    private final String appName;
    private final String instanceId;
    private final long fetchTogglesInterval;
    private final long sendMetricsInterval;

    public UnleashConfig(
            URI unleashAPI,
            String appName,
            String instanceId,
            long fetchTogglesInterval,
            long sendMetricsInterval) {

        if(appName == null) {
            throw new IllegalStateException("You are required to specify the unleash appName");
        }

        if(unleashAPI == null) {
            throw new IllegalStateException("You are required to specify the unleashAPI url");
        }

        this.unleashAPI = unleashAPI;
        this.unleashURLs = new UnleashURLs(unleashAPI);
        this.appName = appName;
        this.instanceId = instanceId;
        this.fetchTogglesInterval = fetchTogglesInterval;
        this.sendMetricsInterval = sendMetricsInterval;
    }

    public URI getUnleashAPI() {
        return unleashAPI;
    }

    public String getAppName() {
        return appName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public long getFetchTogglesInterval() {
        return fetchTogglesInterval;
    }

    public long getSendMetricsInterval() {
        return sendMetricsInterval;
    }

    public UnleashURLs getUnleashURLs() {
        return unleashURLs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private URI unleashAPI;
        private String appName;
        private String instanceId = "generated-"+Math.round(Math.random() * 1000000);
        private long fetchTogglesInterval = 10;
        private long sendMetricsInterval = 60;


        public Builder unleashAPI(URI unleashAPI) {
            this.unleashAPI = unleashAPI;
            return this;
        }

        public Builder unleashAPI(String unleashAPI) {
            this.unleashAPI = URI.create(unleashAPI);
            return this;
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder fetchTogglesInterval(long fetchTogglesInterval) {
            this.fetchTogglesInterval = fetchTogglesInterval;
            return this;
        }

        public Builder sendMetricsInterval(long sendMetricsInterval) {
            this.sendMetricsInterval = sendMetricsInterval;
            return this;
        }

        public UnleashConfig build() {
            return new UnleashConfig(unleashAPI, appName, instanceId, fetchTogglesInterval, sendMetricsInterval);
        }
    }
}
