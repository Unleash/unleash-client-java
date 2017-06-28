package no.finn.unleash.util;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.UnleashContextProvider;

public class UnleashConfig {
    static final String UNLEASH_APP_NAME_HEADER = "UNLEASH-APPNAME";
    static final String UNLEASH_INSTANCE_ID_HEADER = "UNLEASH-INSTANCEID";

    private final URI unleashAPI;
    private final UnleashURLs unleashURLs;
    private final Map<String, String> customHttpHeaders;
    private final String appName;
    private final String instanceId;
    private final String sdkVersion;
    private final String backupFile;
    private final long fetchTogglesInterval;
    private final long sendMetricsInterval;
    private final boolean disableMetrics;
    private final UnleashContextProvider contextProvider;

    public UnleashConfig(
            URI unleashAPI,
            Map<String, String> customHttpHeaders,
            String appName,
            String instanceId,
            String sdkVersion,
            String backupFile,
            long fetchTogglesInterval,
            long sendMetricsInterval,
            boolean disableMetrics,
            UnleashContextProvider contextProvider) {


        if(appName == null) {
            throw new IllegalStateException("You are required to specify the unleash appName");
        }

        if(unleashAPI == null) {
            throw new IllegalStateException("You are required to specify the unleashAPI url");
        }

        this.unleashAPI = unleashAPI;
        this.customHttpHeaders = customHttpHeaders;
        this.unleashURLs = new UnleashURLs(unleashAPI);
        this.appName = appName;
        this.instanceId = instanceId;
        this.sdkVersion = sdkVersion;
        this.backupFile = backupFile;
        this.fetchTogglesInterval = fetchTogglesInterval;
        this.sendMetricsInterval = sendMetricsInterval;
        this.disableMetrics = disableMetrics;
        this.contextProvider = contextProvider;
    }

    public URI getUnleashAPI() {
        return unleashAPI;
    }

    public Map<String, String> getCustomHttpHeaders() {
        return customHttpHeaders;
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

    public boolean isDisableMetrics() {
        return disableMetrics;
    }

    public String getBackupFile() {
        return this.backupFile;
    }

    public UnleashContextProvider getContextProvider() {
        return contextProvider;
    }

    public static void setRequestProperties(HttpURLConnection connection, UnleashConfig config) {
        connection.setRequestProperty(UNLEASH_APP_NAME_HEADER, config.getAppName());
        connection.setRequestProperty(UNLEASH_INSTANCE_ID_HEADER, config.getInstanceId());
        connection.setRequestProperty("User-Agent", config.getAppName());
        config.getCustomHttpHeaders().forEach(connection::setRequestProperty);
    }

    public static class Builder {
        private URI unleashAPI;
        private Map<String, String> customHttpHeaders = new HashMap<>();
        private String appName;
        private String instanceId = getDefaultInstanceId();
        private String sdkVersion = getDefaultSdkVersion();
        private String backupFile;
        private long fetchTogglesInterval = 10;
        private long sendMetricsInterval = 60;
        private boolean disableMetrics = false;
        private UnleashContextProvider contextProvider = UnleashContextProvider.getDefaultProvider();

        static String getDefaultInstanceId() {
            String hostName = "";
            try {
                hostName = InetAddress.getLocalHost().getHostName() + "-";
            } catch (UnknownHostException e) {

            }
            return hostName + "generated-" + Math.round(Math.random() * 1000000.0D);
        }

        public Builder unleashAPI(URI unleashAPI) {
            this.unleashAPI = unleashAPI;
            return this;
        }

        public Builder unleashAPI(String unleashAPI) {
            this.unleashAPI = URI.create(unleashAPI);
            return this;
        }

        public Builder customHttpHeader(String name, String value) {
            this.customHttpHeaders.put(name, value);
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

        public Builder disableMetrics() {
            this.disableMetrics = true;
            return this;
        }

        public Builder backupFile(String backupFile) {
            this.backupFile = backupFile;
            return this;
        }

        public Builder unleashContextProvider(UnleashContextProvider contextProvider) {
            this.contextProvider = contextProvider;
            return this;
        }

        private String getBackupFile() {
            if(backupFile != null) {
                return backupFile;
            } else {
                String fileName = "unleash-" + appName + "-repo.json";
                return System.getProperty("java.io.tmpdir") + File.separatorChar + fileName;
            }
        }

        public UnleashConfig build() {
            return new UnleashConfig(
                    unleashAPI,
                    customHttpHeaders,
                    appName,
                    instanceId,
                    sdkVersion,
                    getBackupFile(),
                    fetchTogglesInterval,
                    sendMetricsInterval,
                    disableMetrics,
                    contextProvider);
        }

        public String getDefaultSdkVersion() {
            String version = Optional.ofNullable(getClass().getPackage().getImplementationVersion())
                    .orElse("development");
            return "unleash-client-java:" + version;
        }
    }
}
