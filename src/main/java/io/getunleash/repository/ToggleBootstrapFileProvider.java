package io.getunleash.repository;

import io.getunleash.lang.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleBootstrapFileProvider implements ToggleBootstrapProvider {
    private static final Logger LOG = LoggerFactory.getLogger(ToggleBootstrapFileProvider.class);
    final String path;

    public ToggleBootstrapFileProvider() {
        this.path = getBootstrapFile();
    }

    /**
     * Accepts path to file to read either as constructor parameter or as
     * environment variable in
     * "UNLEASH_BOOTSTRAP_FILE"
     *
     * @param path - path to toggles file
     */
    public ToggleBootstrapFileProvider(String path) {
        this.path = path;
    }

    @Override
    public Optional<String> read() {
        LOG.info("Trying to read feature toggles from bootstrap file found at {}", path);
        try {
            File file = getFile(path);
            if (file != null) {
                return Optional.of(fileAsString(file));
            }
        } catch (FileNotFoundException ioEx) {
            LOG.warn("Could not find file {}", path, ioEx);
        } catch (IOException ioEx) {
            LOG.warn("Generic IOException when trying to read file at {}", path, ioEx);
        }
        return Optional.empty();
    }

    @Nullable
    private String getBootstrapFile() {
        String path = System.getenv("UNLEASH_BOOTSTRAP_FILE");
        if (path == null) {
            path = System.getProperty("UNLEASH_BOOTSTRAP_FILE");
        }
        return path;
    }

    private String fileAsString(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    @Nullable
    private File getFile(@Nullable String path) {
        if (path != null) {
            if (path.startsWith("classpath:")) {
                try {
                    URL resource = getClass()
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
        } else {
            return null;
        }
    }
}
