package io.getunleash.util;

import static io.getunleash.DefaultUnleash.UNKNOWN_STRATEGY;

import io.getunleash.CustomHttpHeadersProvider;
import io.getunleash.DefaultCustomHttpHeadersProviderImpl;
import io.getunleash.UnleashContextProvider;
import io.getunleash.event.NoOpSubscriber;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.lang.Nullable;
import io.getunleash.metric.DefaultHttpMetricsSender;
import io.getunleash.repository.HttpFeatureFetcher;
import io.getunleash.repository.ToggleBootstrapProvider;
import io.getunleash.strategy.Strategy;
import java.io.File;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UnleashConfig {

    public static final String UNLEASH_APP_NAME_HEADER = "UNLEASH-APPNAME";
    public static final String UNLEASH_INSTANCE_ID_HEADER = "UNLEASH-INSTANCEID";

    private final URI unleashAPI;
    private final UnleashURLs unleashURLs;
    private final Map<String, String> customHttpHeaders;
    private final CustomHttpHeadersProvider customHttpHeadersProvider;
    private final String appName;
    private final String environment;
    private final String instanceId;
    private final String sdkVersion;
    private final String backupFile;

    private final String clientSpecificationVersion;
    @Nullable private final String projectName;
    @Nullable private final String namePrefix;
    private final long fetchTogglesInterval;

    private final Duration fetchTogglesConnectTimeout;

    private final Duration fetchTogglesReadTimeout;

    private final boolean disablePolling;
    private final long sendMetricsInterval;

    private final Duration sendMetricsConnectTimeout;

    private final Duration sendMetricsReadTimeout;
    private final boolean disableMetrics;
    private final boolean isProxyAuthenticationByJvmProperties;
    private final UnleashFeatureFetcherFactory unleashFeatureFetcherFactory;

    private final MetricSenderFactory metricSenderFactory;

    private final UnleashContextProvider contextProvider;
    private final boolean synchronousFetchOnInitialisation;
    private final UnleashScheduledExecutor unleashScheduledExecutor;
    private final UnleashSubscriber unleashSubscriber;
    @Nullable private final Strategy fallbackStrategy;
    @Nullable private final ToggleBootstrapProvider toggleBootstrapProvider;
    @Nullable private final Proxy proxy;

    private UnleashConfig(
            @Nullable URI unleashAPI,
            Map<String, String> customHttpHeaders,
            CustomHttpHeadersProvider customHttpHeadersProvider,
            @Nullable String appName,
            String environment,
            @Nullable String instanceId,
            String sdkVersion,
            String backupFile,
            @Nullable String projectName,
            @Nullable String namePrefix,
            long fetchTogglesInterval,
            Duration fetchTogglesConnectTimeout,
            Duration fetchTogglesReadTimeout,
            boolean disablePolling,
            long sendMetricsInterval,
            Duration sendMetricsConnectTimeout,
            Duration sendMetricsReadTimeout,
            boolean disableMetrics,
            UnleashContextProvider contextProvider,
            boolean isProxyAuthenticationByJvmProperties,
            boolean synchronousFetchOnInitialisation,
            UnleashFeatureFetcherFactory unleashFeatureFetcherFactory,
            MetricSenderFactory metricSenderFactory,
            @Nullable UnleashScheduledExecutor unleashScheduledExecutor,
            @Nullable UnleashSubscriber unleashSubscriber,
            @Nullable Strategy fallbackStrategy,
            @Nullable ToggleBootstrapProvider unleashBootstrapProvider,
            @Nullable Proxy proxy,
            @Nullable Authenticator proxyAuthenticator) {

        if (appName == null) {
            throw new IllegalStateException("You are required to specify the unleash appName");
        }

        if (instanceId == null) {
            throw new IllegalStateException("You are required to specify the unleash instanceId");
        }

        if (unleashAPI == null) {
            throw new IllegalStateException("You are required to specify the unleashAPI url");
        }

        if (unleashScheduledExecutor == null) {
            throw new IllegalStateException("You are required to specify a scheduler");
        }

        if (unleashSubscriber == null) {
            throw new IllegalStateException("You are required to specify a subscriber");
        }

        if (fallbackStrategy == null) {
            this.fallbackStrategy = UNKNOWN_STRATEGY;
        } else {
            this.fallbackStrategy = fallbackStrategy;
        }

        if (isProxyAuthenticationByJvmProperties && proxyAuthenticator == null) {
            enableProxyAuthentication();
        } else if (proxyAuthenticator != null) {
            Authenticator.setDefault(proxyAuthenticator);
        }

        this.unleashAPI = unleashAPI;
        this.customHttpHeaders = customHttpHeaders;
        this.customHttpHeadersProvider = customHttpHeadersProvider;
        this.unleashURLs = new UnleashURLs(unleashAPI);
        this.appName = appName;
        this.environment = environment;
        this.instanceId = instanceId;
        this.sdkVersion = sdkVersion;
        this.backupFile = backupFile;
        this.projectName = projectName;
        this.namePrefix = namePrefix;
        this.fetchTogglesInterval = fetchTogglesInterval;
        this.fetchTogglesConnectTimeout = fetchTogglesConnectTimeout;
        this.fetchTogglesReadTimeout = fetchTogglesReadTimeout;
        this.disablePolling = disablePolling;
        this.sendMetricsInterval = sendMetricsInterval;
        this.sendMetricsConnectTimeout = sendMetricsConnectTimeout;
        this.sendMetricsReadTimeout = sendMetricsReadTimeout;
        this.disableMetrics = disableMetrics;
        this.contextProvider = contextProvider;
        this.isProxyAuthenticationByJvmProperties = isProxyAuthenticationByJvmProperties;
        this.synchronousFetchOnInitialisation = synchronousFetchOnInitialisation;
        this.unleashScheduledExecutor = unleashScheduledExecutor;
        this.unleashSubscriber = unleashSubscriber;
        this.toggleBootstrapProvider = unleashBootstrapProvider;
        this.proxy = proxy;
        this.unleashFeatureFetcherFactory = unleashFeatureFetcherFactory;
        this.metricSenderFactory = metricSenderFactory;
        this.clientSpecificationVersion =
                UnleashProperties.getProperty("client.specification.version");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void setRequestProperties(HttpURLConnection connection, UnleashConfig config) {
        connection.setRequestProperty(UNLEASH_APP_NAME_HEADER, config.getAppName());
        connection.setRequestProperty(UNLEASH_INSTANCE_ID_HEADER, config.getInstanceId());
        connection.setRequestProperty("User-Agent", config.getAppName());
        connection.setRequestProperty(
                "Unleash-Client-Spec", config.getClientSpecificationVersion());
        config.getCustomHttpHeaders().forEach(connection::setRequestProperty);
        config.customHttpHeadersProvider.getCustomHeaders().forEach(connection::setRequestProperty);
    }

    private void enableProxyAuthentication() {
        // http.proxyUser http.proxyPassword is only consumed by Apache HTTP Client, for
        // HttpUrlConnection we have to define an Authenticator
        Authenticator.setDefault(new SystemProxyAuthenticator());
    }

    public URI getUnleashAPI() {
        return unleashAPI;
    }

    public Map<String, String> getCustomHttpHeaders() {
        return customHttpHeaders;
    }

    public CustomHttpHeadersProvider getCustomHttpHeadersProvider() {
        return customHttpHeadersProvider;
    }

    public String getAppName() {
        return appName;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getClientSpecificationVersion() {
        return clientSpecificationVersion;
    }

    public @Nullable String getProjectName() {
        return projectName;
    }

    public long getFetchTogglesInterval() {
        return fetchTogglesInterval;
    }

    public Duration getFetchTogglesConnectTimeout() {
        return fetchTogglesConnectTimeout;
    }

    public Duration getFetchTogglesReadTimeout() {
        return fetchTogglesReadTimeout;
    }

    public boolean isDisablePolling() {
        return disablePolling;
    }

    public long getSendMetricsInterval() {
        return sendMetricsInterval;
    }

    public Duration getSendMetricsConnectTimeout() {
        return sendMetricsConnectTimeout;
    }

    public Duration getSendMetricsReadTimeout() {
        return sendMetricsReadTimeout;
    }

    public UnleashURLs getUnleashURLs() {
        return unleashURLs;
    }

    public boolean isDisableMetrics() {
        return disableMetrics;
    }

    public String getBackupFile() {
        return this.backupFile;
    }

    public boolean isSynchronousFetchOnInitialisation() {
        return synchronousFetchOnInitialisation;
    }

    public UnleashContextProvider getContextProvider() {
        return contextProvider;
    }

    public UnleashScheduledExecutor getScheduledExecutor() {
        return unleashScheduledExecutor;
    }

    public UnleashSubscriber getSubscriber() {
        return unleashSubscriber;
    }

    public boolean isProxyAuthenticationByJvmProperties() {
        return isProxyAuthenticationByJvmProperties;
    }

    @Nullable
    public Strategy getFallbackStrategy() {
        return fallbackStrategy;
    }

    @Nullable
    public ToggleBootstrapProvider getToggleBootstrapProvider() {
        return toggleBootstrapProvider;
    }

    @Nullable
    public String getNamePrefix() {
        return namePrefix;
    }

    @Nullable
    public Proxy getProxy() {
        return proxy;
    }

    public MetricSenderFactory getMetricSenderFactory() {
        return this.metricSenderFactory;
    }

    public UnleashFeatureFetcherFactory getUnleashFeatureFetcherFactory() {
        return this.unleashFeatureFetcherFactory;
    }

    static class SystemProxyAuthenticator extends Authenticator {
        @Override
        protected @Nullable PasswordAuthentication getPasswordAuthentication() {
            if (getRequestorType() == RequestorType.PROXY) {
                final String proto = getRequestingProtocol().toLowerCase();
                final String proxyHost = System.getProperty(proto + ".proxyHost", "");
                final String proxyPort = System.getProperty(proto + ".proxyPort", "");
                final String proxyUser = System.getProperty(proto + ".proxyUser", "");
                final String proxyPassword = System.getProperty(proto + ".proxyPassword", "");

                // Only apply PasswordAuthentication to requests to the proxy itself - if not set
                // just ignore
                if (getRequestingHost().equalsIgnoreCase(proxyHost)
                        && Integer.parseInt(proxyPort) == getRequestingPort()) {
                    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                }
            }
            return null;
        }
    }

    static class CustomProxyAuthenticator extends Authenticator {

        private final Proxy proxy;
        private final String proxyUser;
        private final String proxyPassword;

        public CustomProxyAuthenticator(Proxy proxy, String proxyUser, String proxyPassword) {
            this.proxy = proxy;
            this.proxyUser = proxyUser;
            this.proxyPassword = proxyPassword;
        }

        @Override
        protected @Nullable PasswordAuthentication getPasswordAuthentication() {
            if (getRequestorType() == RequestorType.PROXY
                    && proxy.type() == Proxy.Type.HTTP
                    && proxy.address() instanceof InetSocketAddress) {
                final String proxyHost = ((InetSocketAddress) proxy.address()).getHostName();
                final int proxyPort = ((InetSocketAddress) proxy.address()).getPort();

                // Only apply PasswordAuthentication to requests to the proxy
                // itself - if not set
                // just ignore
                if (getRequestingHost().equalsIgnoreCase(proxyHost)
                        && proxyPort == getRequestingPort()) {
                    return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                }
            }
            return null;
        }
    }

    public static class Builder {

        private @Nullable URI unleashAPI;
        private Map<String, String> customHttpHeaders = new HashMap<>();
        private CustomHttpHeadersProvider customHttpHeadersProvider =
                new DefaultCustomHttpHeadersProviderImpl();
        private @Nullable String appName;
        private String environment = "default";
        private String instanceId = getDefaultInstanceId();
        private final String sdkVersion = getDefaultSdkVersion();
        private @Nullable String backupFile;
        private @Nullable String projectName;
        private @Nullable String namePrefix;
        private long fetchTogglesInterval = 10;

        private Duration fetchTogglesConnectTimeout = Duration.ofSeconds(10);

        private Duration fetchTogglesReadTimeout = Duration.ofSeconds(10);

        private boolean disablePolling = false;
        private long sendMetricsInterval = 60;

        private Duration sendMetricsConnectTimeout = Duration.ofSeconds(10);

        private Duration sendMetricsReadTimeout = Duration.ofSeconds(10);
        private boolean disableMetrics = false;
        private UnleashFeatureFetcherFactory unleashFeatureFetcherFactory = HttpFeatureFetcher::new;

        private MetricSenderFactory unleashMetricSenderFactory = DefaultHttpMetricsSender::new;
        private UnleashContextProvider contextProvider =
                UnleashContextProvider.getDefaultProvider();
        private boolean synchronousFetchOnInitialisation = false;
        private @Nullable UnleashScheduledExecutor scheduledExecutor;
        private @Nullable UnleashSubscriber unleashSubscriber;
        private boolean isProxyAuthenticationByJvmProperties;
        private @Nullable Strategy fallbackStrategy;
        private @Nullable ToggleBootstrapProvider toggleBootstrapProvider;
        private @Nullable Proxy proxy;
        private @Nullable Authenticator proxyAuthenticator;

        private static String getHostname() {
            String hostName = System.getProperty("hostname");
            if (hostName == null || hostName.length() == 0) {
                try {
                    hostName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException e) {
                }
            }
            return hostName + "-";
        }

        static String getDefaultInstanceId() {
            return getHostname() + "generated-" + Math.round(Math.random() * 1000000.0D);
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

        public Builder customHttpHeadersProvider(CustomHttpHeadersProvider provider) {
            this.customHttpHeadersProvider = provider;
            return this;
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }

        public Builder namePrefix(String namePrefix) {
            this.namePrefix = namePrefix;
            return this;
        }

        public Builder unleashFeatureFetcherFactory(
                UnleashFeatureFetcherFactory unleashFeatureFetcherFactory) {
            this.unleashFeatureFetcherFactory = unleashFeatureFetcherFactory;
            return this;
        }

        public Builder metricsSenderFactory(MetricSenderFactory metricSenderFactory) {
            this.unleashMetricSenderFactory = metricSenderFactory;
            return this;
        }

        public Builder fetchTogglesInterval(long fetchTogglesInterval) {
            this.fetchTogglesInterval = fetchTogglesInterval;
            return this;
        }

        public Builder fetchTogglesConnectTimeout(Duration connectTimeout) {
            this.fetchTogglesConnectTimeout = connectTimeout;
            return this;
        }

        public Builder fetchTogglesConnectTimeoutSeconds(long connectTimeoutSeconds) {
            this.fetchTogglesConnectTimeout = Duration.ofSeconds(connectTimeoutSeconds);
            return this;
        }

        public Builder fetchTogglesReadTimeout(Duration readTimeout) {
            this.fetchTogglesReadTimeout = readTimeout;
            return this;
        }

        public Builder fetchTogglesReadTimeoutSeconds(long readTimeoutSeconds) {
            this.fetchTogglesReadTimeout = Duration.ofSeconds(readTimeoutSeconds);
            return this;
        }

        public Builder sendMetricsInterval(long sendMetricsInterval) {
            this.sendMetricsInterval = sendMetricsInterval;
            return this;
        }

        /***
         * Don't poll for feature toggle updates
         */
        public Builder disablePolling() {
            this.disablePolling = true;
            return this;
        }

        public Builder sendMetricsConnectTimeout(Duration connectTimeout) {
            this.sendMetricsConnectTimeout = connectTimeout;
            return this;
        }

        public Builder sendMetricsConnectTimeoutSeconds(long connectTimeoutSeconds) {
            this.sendMetricsConnectTimeout = Duration.ofSeconds(connectTimeoutSeconds);
            return this;
        }

        public Builder sendMetricsReadTimeout(Duration readTimeout) {
            this.sendMetricsReadTimeout = readTimeout;
            return this;
        }

        public Builder sendMetricsReadTimeoutSeconds(long readTimeoutSeconds) {
            this.sendMetricsReadTimeout = Duration.ofSeconds(readTimeoutSeconds);
            return this;
        }

        /**
         * Don't send metrics to Unleash server
         *
         * @return
         */
        public Builder disableMetrics() {
            this.disableMetrics = true;
            return this;
        }

        public Builder backupFile(String backupFile) {
            this.backupFile = backupFile;
            return this;
        }

        public Builder enableProxyAuthenticationByJvmProperties() {
            this.isProxyAuthenticationByJvmProperties = true;
            return this;
        }

        public Builder unleashContextProvider(UnleashContextProvider contextProvider) {
            this.contextProvider = contextProvider;
            return this;
        }

        public Builder synchronousFetchOnInitialisation(boolean enable) {
            this.synchronousFetchOnInitialisation = enable;
            return this;
        }

        public Builder scheduledExecutor(UnleashScheduledExecutor scheduledExecutor) {
            this.scheduledExecutor = scheduledExecutor;
            return this;
        }

        public Builder subscriber(UnleashSubscriber unleashSubscriber) {
            this.unleashSubscriber = unleashSubscriber;
            return this;
        }

        public Builder fallbackStrategy(@Nullable Strategy fallbackStrategy) {
            this.fallbackStrategy = fallbackStrategy;
            return this;
        }

        public Builder toggleBootstrapProvider(
                @Nullable ToggleBootstrapProvider toggleBootstrapProvider) {
            this.toggleBootstrapProvider = toggleBootstrapProvider;
            return this;
        }

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxy(
                Proxy proxy, @Nullable String proxyUser, @Nullable String proxyPassword) {
            this.proxy = proxy;

            if (proxyUser != null && proxyPassword != null) {
                this.proxyAuthenticator =
                        new CustomProxyAuthenticator(proxy, proxyUser, proxyPassword);
            }
            return this;
        }

        private String getBackupFile() {
            if (backupFile != null) {
                return backupFile;
            } else {
                String fileName = "unleash-" + appName + "-repo.json";
                return System.getProperty("java.io.tmpdir") + File.separatorChar + fileName;
            }
        }

        /**
         * Adds a custom http header for authorizing the client
         *
         * @param apiKey the client key to use to connect to the Unleash Server
         * @return
         */
        public Builder apiKey(String apiKey) {
            this.customHttpHeaders.put("Authorization", apiKey);
            return this;
        }

        public UnleashConfig build() {
            return new UnleashConfig(
                    unleashAPI,
                    customHttpHeaders,
                    customHttpHeadersProvider,
                    appName,
                    environment,
                    instanceId,
                    sdkVersion,
                    getBackupFile(),
                    projectName,
                    namePrefix,
                    fetchTogglesInterval,
                    fetchTogglesConnectTimeout,
                    fetchTogglesReadTimeout,
                    disablePolling,
                    sendMetricsInterval,
                    sendMetricsConnectTimeout,
                    sendMetricsReadTimeout,
                    disableMetrics,
                    contextProvider,
                    isProxyAuthenticationByJvmProperties,
                    synchronousFetchOnInitialisation,
                    unleashFeatureFetcherFactory,
                    unleashMetricSenderFactory,
                    Optional.ofNullable(scheduledExecutor)
                            .orElseGet(UnleashScheduledExecutorImpl::getInstance),
                    Optional.ofNullable(unleashSubscriber).orElseGet(NoOpSubscriber::new),
                    fallbackStrategy,
                    toggleBootstrapProvider,
                    proxy,
                    proxyAuthenticator);
        }

        public String getDefaultSdkVersion() {
            String version =
                    Optional.ofNullable(getClass().getPackage().getImplementationVersion())
                            .orElse("development");
            return "unleash-client-java:" + version;
        }
    }
}
