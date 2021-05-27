package no.finn.unleash.util;

import static no.finn.unleash.DefaultUnleash.UNKNOWN_STRATEGY;

import java.io.File;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import no.finn.unleash.CustomHttpHeadersProvider;
import no.finn.unleash.DefaultCustomHttpHeadersProviderImpl;
import no.finn.unleash.UnleashContextProvider;
import no.finn.unleash.event.NoOpSubscriber;
import no.finn.unleash.event.UnleashSubscriber;
import no.finn.unleash.lang.Nullable;
import no.finn.unleash.repository.ToggleBootstrapProvider;
import no.finn.unleash.strategy.Strategy;

public class UnleashConfig {

    static final String UNLEASH_APP_NAME_HEADER = "UNLEASH-APPNAME";
    static final String UNLEASH_INSTANCE_ID_HEADER = "UNLEASH-INSTANCEID";

    private final URI unleashAPI;
    private final UnleashURLs unleashURLs;
    private final Map<String, String> customHttpHeaders;
    private final CustomHttpHeadersProvider customHttpHeadersProvider;
    private final String appName;
    private final String environment;
    private final String instanceId;
    private final String sdkVersion;
    private final String backupFile;
    @Nullable private final String projectName;
    private final long fetchTogglesInterval;
    private final long sendMetricsInterval;
    private final boolean disableMetrics;
    private final boolean isProxyAuthenticationByJvmProperties;
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
            long fetchTogglesInterval,
            long sendMetricsInterval,
            boolean disableMetrics,
            UnleashContextProvider contextProvider,
            boolean isProxyAuthenticationByJvmProperties,
            boolean synchronousFetchOnInitialisation,
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
        this.fetchTogglesInterval = fetchTogglesInterval;
        this.sendMetricsInterval = sendMetricsInterval;
        this.disableMetrics = disableMetrics;
        this.contextProvider = contextProvider;
        this.isProxyAuthenticationByJvmProperties = isProxyAuthenticationByJvmProperties;
        this.synchronousFetchOnInitialisation = synchronousFetchOnInitialisation;
        this.unleashScheduledExecutor = unleashScheduledExecutor;
        this.unleashSubscriber = unleashSubscriber;
        this.toggleBootstrapProvider = unleashBootstrapProvider;
        this.proxy = proxy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void setRequestProperties(HttpURLConnection connection, UnleashConfig config) {
        connection.setRequestProperty(UNLEASH_APP_NAME_HEADER, config.getAppName());
        connection.setRequestProperty(UNLEASH_INSTANCE_ID_HEADER, config.getInstanceId());
        connection.setRequestProperty("User-Agent", config.getAppName());
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

    public @Nullable String getProjectName() {
        return projectName;
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
    public Proxy getProxy() {
        return proxy;
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
                if (getRequestingHost().equalsIgnoreCase(proxyHost) && Integer.parseInt(proxyPort) == getRequestingPort()) {
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
            if (getRequestorType() == RequestorType.PROXY && proxy.type() == Proxy.Type.HTTP && proxy.address() instanceof InetSocketAddress) {
                final String proxyHost = ((InetSocketAddress) proxy.address()).getHostName();
                final int proxyPort = ((InetSocketAddress) proxy.address()).getPort();

                // Only apply PasswordAuthentication to requests to the proxy
                // itself - if not set
                // just ignore
                if (getRequestingHost().equalsIgnoreCase(proxyHost) && proxyPort == getRequestingPort()) {
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
        private String sdkVersion = getDefaultSdkVersion();
        private @Nullable String backupFile;
        private @Nullable String projectName;
        private long fetchTogglesInterval = 10;
        private long sendMetricsInterval = 60;
        private boolean disableMetrics = false;
        private UnleashContextProvider contextProvider =
                UnleashContextProvider.getDefaultProvider();
        private boolean synchronousFetchOnInitialisation = false;
        private @Nullable UnleashScheduledExecutor scheduledExecutor;
        private @Nullable UnleashSubscriber unleashSubscriber;
        private boolean isProxyAuthenticationByJvmProperties;
        private Strategy fallbackStrategy;
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
                this.proxyAuthenticator = new CustomProxyAuthenticator(proxy, proxyUser, proxyPassword);
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
                    fetchTogglesInterval,
                    sendMetricsInterval,
                    disableMetrics,
                    contextProvider,
                    isProxyAuthenticationByJvmProperties,
                    synchronousFetchOnInitialisation,
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
