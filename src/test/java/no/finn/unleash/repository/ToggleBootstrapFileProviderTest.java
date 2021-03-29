package no.finn.unleash.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import org.junit.jupiter.api.Test;

class ToggleBootstrapFileProviderTest {

    @Test
    public void shouldBeAbleToLoadFilePassedInAsArgument() {
        File exampleRepoFile =
                new File(getClass().getClassLoader().getResource("unleash-repo-v0.json").getFile());
        ToggleBootstrapFileProvider toggleBootstrapFileProvider =
                new ToggleBootstrapFileProvider(exampleRepoFile.getAbsolutePath());
        assertThat(toggleBootstrapFileProvider.read()).isNotEmpty();
    }

    @Test
    public void shouldBeAbleToLoadFilePassedInEnvironment() {
        File exampleRepoFile =
                new File(getClass().getClassLoader().getResource("unleash-repo-v0.json").getFile());
        System.setProperty("UNLEASH_BOOTSTRAP_FILE", exampleRepoFile.getAbsolutePath());
        ToggleBootstrapFileProvider toggleBootstrapFileProvider =
                new ToggleBootstrapFileProvider(null);
        assertThat(toggleBootstrapFileProvider.read()).isNotEmpty();
    }

    @Test
    public void shouldBeAbleToLoadfileFromClasspathReference() {
        System.setProperty("UNLEASH_BOOTSTRAP_FILE", "classpath:unleash-repo-v0.json");
        ToggleBootstrapFileProvider bootstrap = new ToggleBootstrapFileProvider(null);

        String read = bootstrap.read();
        assertThat(read).isNotBlank();
    }
}
