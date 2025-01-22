package io.getunleash.repository;

import com.google.gson.JsonParseException;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.util.UnleashConfig;
import java.io.*;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated()
public class ToggleBackupHandlerFile implements BackupHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ToggleBackupHandlerFile.class);

    private final String backupFile;
    private final EventDispatcher eventDispatcher;

    public ToggleBackupHandlerFile(UnleashConfig config) {
        this.backupFile = config.getBackupFile();
        this.eventDispatcher = new EventDispatcher(config);
    }

    @Override
    public Optional<String> read() {
        LOG.info("Unleash will try to load feature toggle states from temporary backup");
        try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
            String clientFeatures = reader.lines().collect(Collectors.joining("\n"));
            eventDispatcher.dispatch(new ToggleBackupRead(clientFeatures));
            return Optional.of(clientFeatures);
        } catch (FileNotFoundException e) {
            LOG.info(
                    " Unleash could not find the backup-file '"
                            + backupFile
                            + "'. \n"
                            + "This is expected behavior the first time unleash runs in a new environment.");
        } catch (IOException | IllegalStateException | JsonParseException e) {
            eventDispatcher.dispatch(
                    new UnleashException("Failed to read backup file: " + backupFile, e));
        }

        return Optional.empty();
    }

    @Override
    public void write(String clientFeatures) {
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(clientFeatures);
            eventDispatcher.dispatch(new ToggleBackupWritten(clientFeatures));
        } catch (IOException e) {
            eventDispatcher.dispatch(
                    new UnleashException(
                            "Unleash was unable to backup feature toggles to file: " + backupFile,
                            e));
        }
    }

    private static class ToggleBackupRead implements UnleashEvent {

        private final String toggleCollection;

        private ToggleBackupRead(String toggleCollection) {
            this.toggleCollection = toggleCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.toggleBackupRestored(toggleCollection);
        }
    }

    private static class ToggleBackupWritten implements UnleashEvent {

        private final String toggleCollection;

        private ToggleBackupWritten(String toggleCollection) {
            this.toggleCollection = toggleCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.togglesBackedUp(toggleCollection);
        }
    }
}
