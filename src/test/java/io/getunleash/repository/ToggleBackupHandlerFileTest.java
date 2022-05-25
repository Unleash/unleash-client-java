package io.getunleash.repository;

import static org.junit.jupiter.api.Assertions.*;

import io.getunleash.TestUtil;
import io.getunleash.util.UnleashConfig;
import java.io.*;
import java.net.URISyntaxException;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

public class ToggleBackupHandlerFileTest {

    @Test
    public void test_read() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://http://unleash.org")
                        .backupFile(getClass().getResource("/unleash-repo-v0.json").getFile())
                        .build();
        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(config);
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();

        assertNotNull(
                toggleCollection.getToggle("presentFeature"), "presentFeature should be present");
    }

    @Test
    public void test_read_file_with_invalid_data() throws Exception {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(
                                getClass()
                                        .getResource("/unleash-repo-without-feature-field.json")
                                        .getFile())
                        .build();

        ToggleBackupHandlerFile fileGivingNullFeature = new ToggleBackupHandlerFile(config);
        assertNotNull(fileGivingNullFeature.read());
    }

    @Test
    public void test_read_without_file() throws URISyntaxException {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile("/does/not/exist.json")
                        .build();

        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(config);
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();

        assertNull(
                toggleCollection.getToggle("presentFeature"),
                "presentFeature should not be present");
    }

    @Test
    public void test_write_strategies() {
        String backupFile =
                System.getProperty("java.io.tmpdir")
                        + File.separatorChar
                        + "unleash-repo-test-write.json";
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(backupFile)
                        .build();

        String staticData =
                "{\"features\": [{\"name\": \"writableFeature\",\"enabled\": true,\"strategy\": \"default\"}]}";
        Reader staticReader = new StringReader(staticData);
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(staticReader);

        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(config);
        toggleBackupHandlerFile.write(toggleCollection);
        toggleBackupHandlerFile = new ToggleBackupHandlerFile(config);
        toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull(
                toggleCollection.getToggle("writableFeature"), "writableFeature should be present");
    }

    @Test
    public void test_read_old_format_with_strategies() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(getClass().getResource("/unleash-repo-v0.json").getFile())
                        .build();

        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(config);
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull(
                toggleCollection.getToggle("featureCustomStrategy"),
                "presentFeature should be present");
        assertEquals(
                toggleCollection.getToggle("featureCustomStrategy").getStrategies().size(),
                1,
                "should have 1 strategy");
        assertEquals(
                toggleCollection
                        .getToggle("featureCustomStrategy")
                        .getStrategies()
                        .get(0)
                        .getParameters()
                        .get("customParameter"),
                "customValue");
    }

    @Test
    public void test_file_is_directory_should_not_crash() {
        TestUtil.setLogLevel(Level.ERROR); // Mute warn messages.

        String backupFileIsDir = System.getProperty("java.io.tmpdir");
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(backupFileIsDir)
                        .build();

        String staticData =
                "{\"features\": [{\"name\": \"writableFeature\",\"enabled\": false,\"strategy\": \"default\"}]}";
        Reader staticReader = new StringReader(staticData);
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(staticReader);

        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(config);

        toggleBackupHandlerFile.write(toggleCollection);
        assertTrue(true, "Did not crash even if backup-writer yields IOException");
    }
}
