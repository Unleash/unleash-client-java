package no.finn.unleash.util;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static no.finn.unleash.util.UnleashConfig.UNLEASH_APP_NAME_HEADER;
import static no.finn.unleash.util.UnleashConfig.UNLEASH_INSTANCE_ID_HEADER;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void should_require_instanceId() {
        Executable ex = () ->  UnleashConfig.builder()
                .appName("my-app")
                .instanceId(null)
                .unleashAPI("http://unleash.org")
                .build();

        assertThrows(IllegalStateException.class, ex);
    }
}
