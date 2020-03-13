package no.finn.unleash.util;

import no.finn.unleash.CustomHttpHeadersProvider;
import no.finn.unleash.DefaultCustomHttpHeadersProviderImpl;
import no.finn.unleash.util.UnleashConfig.ProxyAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.net.Authenticator.RequestorType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;

import static no.finn.unleash.util.UnleashConfig.UNLEASH_APP_NAME_HEADER;
import static no.finn.unleash.util.UnleashConfig.UNLEASH_INSTANCE_ID_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class UnleashConfigTest {

    @Test
    public void should_require_unleasAPI_url() {
        Executable ex = () -> UnleashConfig.builder().appName("test").build();
        assertThrows(IllegalStateException.class, ex);
    }

    @Test
    public void should_require_app_name() {
        Executable ex = () -> UnleashConfig.builder().unleashAPI("http://unleash.com").build();
        assertThrows(IllegalStateException.class, ex);
    }

    @Test
    public void should_require_valid_uri() {
        Executable ex = () -> UnleashConfig.builder().unleashAPI("this is not a uri").build();
        assertThrows(IllegalArgumentException.class, ex);
    }

    @Test
    public void should_build_config() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .instanceId("my-instance-1")
                .unleashAPI("http://unleash.org")
                .build();

        assertThat(config.getAppName(), is("my-app"));
        assertThat(config.getInstanceId(), is("my-instance-1"));
        assertThat(config.getUnleashAPI(), is(URI.create("http://unleash.org")));
    }

    @Test
    public void should_generate_backupfile() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .unleashAPI("http://unleash.org")
                .build();

        assertThat(config.getAppName(), is("my-app"));
        assertThat(config.getBackupFile(), is(System.getProperty("java.io.tmpdir") + File.separatorChar + "unleash-my-app-repo.json"));
    }

    @Test
    public void should_use_provided_backupfile() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .backupFile("/test/unleash-backup.json")
                .unleashAPI("http://unleash.org")
                .build();

        assertThat(config.getAppName(), is("my-app"));
        assertThat(config.getBackupFile(), is("/test/unleash-backup.json"));
    }

    @Test
    public void should_set_sdk_version() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .unleashAPI("http://unleash.org")
                .build();

        assertThat(config.getSdkVersion(), is("unleash-client-java:development"));
    }

    @Test
    public void should_set_environment_to_default() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .unleashAPI("http://unleash.org")
                .build();

        assertThat(config.getEnvironment(), is("default"));
    }

    @Test
    public void should_set_environment() {
        String env = "prod";
        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .environment(env)
                .unleashAPI("http://unleash.org")
                .build();

        assertThat(config.getEnvironment(), is(env));
    }

    @Test
    public void should_add_app_name_and_instance_id_and_user_agent_to_connection() throws IOException {
        String appName = "my-app";
        String instanceId = "my-instance-1";
        String unleashAPI = "http://unleash.org";

        UnleashConfig unleashConfig = UnleashConfig.builder()
                .appName(appName)
                .instanceId(instanceId)
                .unleashAPI(unleashAPI)
                .build();

        URL someUrl = new URL(unleashAPI + "/some/arbitrary/path");
        HttpURLConnection connection = (HttpURLConnection) someUrl.openConnection();

        UnleashConfig.setRequestProperties(connection, unleashConfig);
        assertThat(connection.getRequestProperty(UNLEASH_APP_NAME_HEADER), is(appName));
        assertThat(connection.getRequestProperty(UNLEASH_INSTANCE_ID_HEADER), is(instanceId));
        assertThat(connection.getRequestProperty("User-Agent"), is(appName));
    }

    @Test
    public void should_add_custom_headers_to_connection_if_present() throws IOException {
        String unleashAPI = "http://unleash.org";
        String headerName = "UNLEASH-CUSTOM-TEST-HEADER";
        String headerValue = "Some value";

        UnleashConfig unleashConfig = UnleashConfig.builder()
                .appName("my-app")
                .instanceId("my-instance-1")
                .unleashAPI(unleashAPI)
                .customHttpHeader(headerName, headerValue)
                .build();

        URL someUrl = new URL(unleashAPI + "/some/arbitrary/path");
        HttpURLConnection connection = (HttpURLConnection) someUrl.openConnection();

        UnleashConfig.setRequestProperties(connection, unleashConfig);
        assertThat(connection.getRequestProperty(headerName), is(headerValue));
    }

    @Test
    public void should_add_custom_headers_from_provider_to_connection_if_present() throws IOException {
        String unleashAPI = "http://unleash.org";
        Map<String,String> result = new HashMap() {{ put("PROVIDER-HEADER","Provider Value"); }};

        CustomHttpHeadersProvider provider = Mockito.mock(DefaultCustomHttpHeadersProviderImpl.class);
        when(provider.getCustomHeaders()).thenReturn(result);

        UnleashConfig unleashConfig = UnleashConfig.builder()
                .appName("my-app")
                .instanceId("my-instance-1")
                .unleashAPI(unleashAPI)
                .customHttpHeadersProvider(provider)
                .build();

        URL someUrl = new URL(unleashAPI + "/some/arbitrary/path");
        HttpURLConnection connection = (HttpURLConnection) someUrl.openConnection();

        UnleashConfig.setRequestProperties(connection, unleashConfig);

        for (String key: result.keySet()) {
            assertThat(connection.getRequestProperty(key), is(result.get(key)));
        }
    }

    @Test
    public void should_require_instanceId() {
        Executable ex = () -> UnleashConfig.builder()
                .appName("my-app")
                .instanceId(null)
                .unleashAPI("http://unleash.org")
                .build();

        assertThrows(IllegalStateException.class, ex);
    }

    @Test
    public void should_enable_proxy_based_on_jvm_settings()
            throws IllegalAccessException, NoSuchFieldException {

        String proxyHost = "proxy-host";
        String proxyPort = "8080";
        String proxyUser = "my-proxy-user";
        String proxyPassword = "my-secret";

        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", proxyPort);
        System.setProperty("http.proxyUser", proxyUser);
        System.setProperty("http.proxyPassword", proxyPassword);

        UnleashConfig config = UnleashConfig.builder()
                .appName("my-app")
                .unleashAPI("http://unleash.org")
                .enableProxyAuthenticationByJvmProperties()
                .build();

        Field authenticator = Authenticator.class.getDeclaredField("theAuthenticator");
        authenticator.setAccessible(true);
        ProxyAuthenticator proxyAuthenticator = (ProxyAuthenticator) authenticator.get(null);

        Field requestingAuthType = proxyAuthenticator.getClass().getSuperclass().getDeclaredField("requestingAuthType");
        requestingAuthType.setAccessible(true);
        requestingAuthType.set(proxyAuthenticator, RequestorType.PROXY);

        Field requestingProtocol = proxyAuthenticator.getClass().getSuperclass().getDeclaredField("requestingProtocol");
        requestingProtocol.setAccessible(true);
        requestingProtocol.set(proxyAuthenticator, "http");

        Field requestingHost = proxyAuthenticator.getClass().getSuperclass().getDeclaredField("requestingHost");
        requestingHost.setAccessible(true);
        requestingHost.set(proxyAuthenticator, proxyHost);

        Field requestingPort = proxyAuthenticator.getClass().getSuperclass().getDeclaredField("requestingPort");
        requestingPort.setAccessible(true);
        requestingPort.set(proxyAuthenticator, 8080);

        PasswordAuthentication passwordAuthentication = proxyAuthenticator.getPasswordAuthentication();

        assertThat(passwordAuthentication.getUserName(), is(proxyUser));
        assertThat(new String(passwordAuthentication.getPassword()), is(proxyPassword));
        assertThat(config.isProxyAuthenticationByJvmProperties(), is(true));
    }
}
