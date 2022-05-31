package io.getunleash.repository;

import io.getunleash.TestUtil;
import io.getunleash.util.UnleashConfig;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureBackupHandlerFileTest {

    @Test
    public void test_read() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://http://unleash.org")
                        .backupFile(getClass().getResource("/unleash-repo-v2.json").getFile())
                        .build();
        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);
        FeatureCollection featureCollection = backupHandler.read();

        assertNotNull(
            featureCollection.getToggle("featureX"), "featureX feature should be present");
        assertNotNull(
            featureCollection.getSegment(1), "segment 1 should be present");
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

        FeatureBackupHandlerFile fileGivingNullFeature = new FeatureBackupHandlerFile(config);
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

        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);
        FeatureCollection featureCollection = backupHandler.read();

        assertNull(
            featureCollection.getToggle("presentFeature"),
                "presentFeature should not be present");
    }

    @Test
    public void test_write_strategies() throws InterruptedException {
        String backupFile =
                System.getProperty("java.io.tmpdir")
                        + File.separatorChar
                        + "unleash-repo-v2-test-write.json";
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(backupFile)
                        .build();

        String staticData =
                "{\"version\":2,\"segments\":[{\"id\":1,\"name\":\"some-name\",\"description\":null,\"constraints\":[{\"contextName\":\"some-name\",\"operator\":\"IN\",\"value\":\"name\",\"inverted\":false,\"caseInsensitive\":true}]}],\"features\":[{\"name\":\"Test.variants\",\"description\":null,\"enabled\":true,\"strategies\":[{\"name\":\"default\",\"segments\":[1]}],\"variants\":[{\"name\":\"variant1\",\"weight\":50},{\"name\":\"variant2\",\"weight\":50}],\"createdAt\":\"2019-01-24T10:41:45.236Z\"}]}";
        Reader staticReader = new StringReader(staticData);
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(staticReader);

        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);
        backupHandler.write(featureCollection);
        backupHandler = new FeatureBackupHandlerFile(config);
        featureCollection = backupHandler.read();
        assertNotNull(
            featureCollection.getToggle("Test.variants"), "Test.variants feature should be present");
        assertNotNull(
            featureCollection.getSegment(1), "segment 1 should be present");
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
            "{\"version\":2,\"segments\":[{\"id\":1,\"name\":\"some-name\",\"description\":null,\"constraints\":[{\"contextName\":\"some-name\",\"operator\":\"IN\",\"value\":\"name\",\"inverted\":false,\"caseInsensitive\":true}]}],\"features\":[{\"name\":\"Test.variants\",\"description\":null,\"enabled\":true,\"strategies\":[{\"name\":\"default\",\"segments\":[1]}],\"variants\":[{\"name\":\"variant1\",\"weight\":50},{\"name\":\"variant2\",\"weight\":50}],\"createdAt\":\"2019-01-24T10:41:45.236Z\"}]}";
        Reader staticReader = new StringReader(staticData);
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(staticReader);

        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);

        backupHandler.write(featureCollection);
        assertTrue(true, "Did not crash even if backup-writer yields IOException");
    }
}
