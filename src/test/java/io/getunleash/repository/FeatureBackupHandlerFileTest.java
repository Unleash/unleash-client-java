package io.getunleash.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import io.getunleash.FeatureDefinition;
import io.getunleash.util.ClientFeaturesParser;
import io.getunleash.util.UnleashConfig;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

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
        String clientFeatureJson = backupHandler.read().get();

        List<FeatureDefinition> featureCollection = ClientFeaturesParser.parse(clientFeatureJson);
        Optional<FeatureDefinition> feature =
                featureCollection.stream().filter(f -> f.getName().equals("featureX")).findFirst();

        assertThat(feature).isNotEmpty();
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
        Optional<String> featureCollection = backupHandler.read();

        assertThat(featureCollection).isEmpty();
    }

    @Test
    public void test_read_write_is_symmetrical() throws InterruptedException {
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

        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);
        backupHandler.write(staticData);
        backupHandler = new FeatureBackupHandlerFile(config);
        Optional<String> features = backupHandler.read();
        assertEquals(staticData, features.get());
    }

    @Test
    public void test_file_is_directory_should_not_crash() {
        String backupFileIsDir = System.getProperty("java.io.tmpdir");
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(backupFileIsDir)
                        .build();

        String staticData =
                "{\"version\":2,\"segments\":[{\"id\":1,\"name\":\"some-name\",\"description\":null,\"constraints\":[{\"contextName\":\"some-name\",\"operator\":\"IN\",\"value\":\"name\",\"inverted\":false,\"caseInsensitive\":true}]}],\"features\":[{\"name\":\"Test.variants\",\"description\":null,\"enabled\":true,\"strategies\":[{\"name\":\"default\",\"segments\":[1]}],\"variants\":[{\"name\":\"variant1\",\"weight\":50},{\"name\":\"variant2\",\"weight\":50}],\"createdAt\":\"2019-01-24T10:41:45.236Z\"}]}";

        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);

        backupHandler.write(staticData);
        assertTrue(true, "Did not crash even if backup-writer yields IOException");
    }
}
