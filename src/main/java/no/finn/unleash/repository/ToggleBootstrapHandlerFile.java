package no.finn.unleash.repository;

import com.google.gson.JsonParseException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;
import no.finn.unleash.UnleashException;
import no.finn.unleash.event.EventDispatcher;
import no.finn.unleash.event.UnleashEvent;
import no.finn.unleash.event.UnleashSubscriber;
import no.finn.unleash.lang.Nullable;
import no.finn.unleash.util.UnleashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToggleBootstrapHandlerFile implements ToggleBootstrapHandler {
    @Nullable private final String file;
    @Nullable private final String checksum;
    private final EventDispatcher eventDispatcher;

    public ToggleBootstrapHandlerFile(UnleashConfig config) {
        this.file = getEnvOrProperty("UNLEASH_BOOTSTRAP_FILE");
        this.checksum = getEnvOrProperty("UNLEASH_BOOTSTRAP_FILE_CHECKSUM");
        this.eventDispatcher = new EventDispatcher(config);
    }

    @Nullable
    private String getEnvOrProperty(String envName) {
        return Optional.ofNullable(System.getenv(envName))
                .orElseGet(() -> System.getProperty(envName));
    }

    private static final Logger LOG = LoggerFactory.getLogger(ToggleBootstrapHandlerFile.class);

    @Override
    public ToggleCollection readAndValidate() {
        if (file != null && Files.exists(Paths.get(file))) {
            LOG.info("Unleash will instantiate Toggles from {}", file);
            File togglesFile = Paths.get(file).toFile();
            Optional<String> shaSum = sha256sum(togglesFile);
            shaSum.ifPresent(s -> LOG.info("{} summed to {}", file, s));
            if (this.checksum != null) {
                if (validate(this.checksum, shaSum)) {
                    return parseFile(togglesFile);
                } else {
                    LOG.info(
                            "Expected checksum: {} - Found checksum: {}",
                            this.checksum,
                            shaSum.orElse("notcalculated"));
                    LOG.warn("Cowardly refusing to read bootstrap file due to checksum mismatch");
                }
            } else {
                return parseFile(togglesFile);
            }
        }
        return new ToggleCollection(Collections.emptyList());
    }

    private ToggleCollection parseFile(File togglesFile) {
        try (FileReader reader = new FileReader(togglesFile)) {
            BufferedReader br = new BufferedReader(reader);
            ToggleCollection toggleCollection = JsonToggleParser.fromJson(br);
            eventDispatcher.dispatch(new ToggleBootstrapRead(toggleCollection));
            return toggleCollection;
        } catch (FileNotFoundException fnf) {
            LOG.info(
                    "Unleash could not find the bootstrap file '{}'. Mare sure the file exists at the passed in path",
                    togglesFile.getAbsolutePath());
        } catch (IOException | IllegalStateException | JsonParseException e) {
            eventDispatcher.dispatch(
                    new UnleashException(
                            "Failed to read boostrap file: " + togglesFile.getAbsolutePath(), e));
        }
        return new ToggleCollection(Collections.emptyList());
    }

    @Override
    public boolean validate(String expectedSha, Optional<String> calculatedSha) {
        return calculatedSha.map(expectedSha::equals).orElse(false);
    }

    Optional<String> sha256sum(File file) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            try (InputStream is = new FileInputStream(file)) {
                DigestInputStream dis = new DigestInputStream(is, sha256);
                while (dis.read() != -1)
                    ; // Clear data
                sha256 = dis.getMessageDigest();
            } catch (IOException ioEx) {
                LOG.warn("Could not read file");
                return Optional.empty();
            }
            return Optional.of(shaSumToHex(sha256.digest()));
        } catch (NoSuchAlgorithmException e) {
            LOG.warn("Could not load algorithm sha256", e);
        }
        return Optional.empty();
    }

    private String shaSumToHex(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static class ToggleBootstrapRead implements UnleashEvent {
        private final ToggleCollection toggleCollection;

        private ToggleBootstrapRead(ToggleCollection toggleCollection) {
            this.toggleCollection = toggleCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.togglesBootstrapped(toggleCollection);
        }
    }
}
