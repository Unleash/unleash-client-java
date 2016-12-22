package no.finn.unleash.util;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UnleashConfigTest {

    @Test(expected = IllegalStateException.class)
    public void should_require_unleasAPI_url() {
        UnleashConfig.builder().appName("test").build();
    }

    @Test(expected = IllegalStateException.class)
    public void should_require_app_name() {
        UnleashConfig.builder().unleashAPI("http://unleash.com").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_require_valid_uri() {
        UnleashConfig.builder().unleashAPI("this is not a uri").build();
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
        assertThat(config.getBackupFile(), is(System.getProperty("java.io.tmpdir") + "/unleash-my-app-repo.json"));
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
}