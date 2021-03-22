package no.finn.unleash.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import no.finn.unleash.lang.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleBootstrapFileProvider implements ToggleBootstrapProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ToggleBootstrapFileProvider.class);
    final String path;

    /**
     * Accepts path to file to read either as constructor parameter or as environment variable in
     * "UNLEASH_BOOTSTRAP_FILE"
     *
     * @param path - path to toggles file
     */
    public ToggleBootstrapFileProvider(@Nullable String path) {
        this.path =
                Optional.ofNullable(path)
                        .orElseGet(() -> getEnvOrProperty("UNLEASH_BOOTSTRAP_FILE"));
    }

    @Override
    @Nullable
    public String read() {
        LOG.info("Trying to read feature toggles from bootstrap file found at {}", path);
        try {
            File file = getFile(path);
            return fileAsString(file);
        } catch (FileNotFoundException ioEx) {
            LOG.warn("Could not find file {}", path, ioEx);
        } catch (IOException ioEx) {
            LOG.warn("Generic IOException when trying to read file at {}", path, ioEx);
        }
        return null;
    }

    @Nullable
    private String getEnvOrProperty(String envName) {
        return Optional.ofNullable(System.getenv(envName))
                .orElseGet(() -> System.getProperty(envName));
    }

    private String fileAsString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Nullable
    private File getFile(String path) {
        if (path.startsWith("classpath:")) {
            try {
                URL resource =
                        getClass()
                                .getClassLoader()
                                .getResource(path.substring("classpath:".length()));
                if (resource != null) {
                    return Paths.get(resource.toURI()).toFile();
                }
                return null;
            } catch (URISyntaxException e) {
                return null;
            }
        } else {
            return Paths.get(path).toFile();
        }
    }
}
