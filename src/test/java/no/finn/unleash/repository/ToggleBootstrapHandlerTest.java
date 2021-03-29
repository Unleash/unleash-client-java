package no.finn.unleash.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.Test;

public class ToggleBootstrapHandlerTest {
    @Test
    public void can_parse_toggle_collection_from_string() throws URISyntaxException, IOException {
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v1.json").toURI());
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://http://unleash.org")
                        .build();
        ToggleBootstrapHandler handler = new ToggleBootstrapHandler(config);

        ToggleCollection parse = handler.parse(fileToString(file));
        assertThat(parse.getFeatures()).hasSize(3);
    }

    private String fileToString(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
}
