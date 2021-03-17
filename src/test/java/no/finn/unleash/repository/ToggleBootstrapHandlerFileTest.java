package no.finn.unleash.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.security.NoSuchAlgorithmException;

import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.Test;

class ToggleBootstrapHandlerFileTest {

    private UnleashConfig defaultConfig =
            UnleashConfig.builder()
                    .appName("test-unleash")
                    .unleashAPI("http://localhost:3000")
                    .build();
    private String expectedHash =
            "e2447a5d043c3d0b180cee0cbdf6633d8010a1149137fb1869fb075970c568ae";

    @Test
    public void shouldHashFileCorrectly() throws NoSuchAlgorithmException {
        ToggleBootstrapHandlerFile bootstrap = new ToggleBootstrapHandlerFile(defaultConfig);
        File exampleRepoFile =
                new File(getClass().getClassLoader().getResource("unleash-repo-v0.json").getFile());
        try {
            assertThat(bootstrap.sha256sum(exampleRepoFile)).hasValue(expectedHash);
        } catch(NoSuchAlgorithmException nsa) {

        }
    }

    @Test
    public void shouldBeAbleToLoadFileFromDisk() {
        File exampleRepoFile =
                new File(getClass().getClassLoader().getResource("unleash-repo-v0.json").getFile());
        System.setProperty("UNLEASH_BOOTSTRAP_FILE", exampleRepoFile.getAbsolutePath());
        System.setProperty("UNLEASH_BOOTSTRAP_FILE_CHECKSUM", expectedHash);
        ToggleBootstrapHandler bootstrap = new ToggleBootstrapHandlerFile(defaultConfig);

        ToggleCollection collection = bootstrap.readAndValidate();
        assertThat(collection.getFeatures()).hasSize(4);
    }

    @Test
    public void shouldBeAbleToLoadfileFromClasspathReference() {
        System.setProperty("UNLEASH_BOOTSTRAP_FILE", "classpath:unleash-repo-v0.json");
        System.setProperty("UNLEASH_BOOTSTRAP_FILE_CHECKSUM", expectedHash);
        ToggleBootstrapHandler bootstrap = new ToggleBootstrapHandlerFile(defaultConfig);

        ToggleCollection collection = bootstrap.readAndValidate();
        assertThat(collection.getFeatures()).hasSize(4);
    }

    @Test
    public void shouldNotLoadIfChecksumOfFileAndExpectedChecksumDoesNotMatch() {
        File exampleRepoFile =
                new File(getClass().getClassLoader().getResource("unleash-repo-v0.json").getFile());
        System.setProperty("UNLEASH_BOOTSTRAP_FILE", exampleRepoFile.getAbsolutePath());
        System.setProperty("UNLEASH_BOOTSTRAP_FILE_CHECKSUM", "wrongvalue");
        ToggleBootstrapHandler bootstrap = new ToggleBootstrapHandlerFile(defaultConfig);
        ToggleCollection collection = bootstrap.readAndValidate();
        assertThat(collection.getFeatures()).isEmpty();
    }

    @Test
    public void loadsWithoutCheckingShasumIfNoChecksumIsSet() {
        File exampleRepoFile =
                new File(getClass().getClassLoader().getResource("unleash-repo-v0.json").getFile());
        System.setProperty("UNLEASH_BOOTSTRAP_FILE", exampleRepoFile.getAbsolutePath());
        ToggleBootstrapHandler bootstrap = new ToggleBootstrapHandlerFile(defaultConfig);
        ToggleCollection collection = bootstrap.readAndValidate();
        assertThat(collection.getFeatures()).hasSize(4);
    }
}
