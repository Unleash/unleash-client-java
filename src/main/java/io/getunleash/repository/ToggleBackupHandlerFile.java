package io.getunleash.repository;

import com.google.gson.JsonParseException;
import io.getunleash.FeatureToggle;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.util.UnleashConfig;
import java.io.*;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated()
public class ToggleBackupHandlerFile implements BackupHandler<ToggleCollection> {
    private static final Logger LOG = LoggerFactory.getLogger(ToggleBackupHandlerFile.class);

    private final String backupFile;
    private final EventDispatcher eventDispatcher;

    public ToggleBackupHandlerFile(UnleashConfig config) {
        this.backupFile = config.getBackupFile();
        this.eventDispatcher = new EventDispatcher(config);
    }

    @Override
    public ToggleCollection read() {
        LOG.info("Unleash will try to load feature toggle states from temporary backup");
        try (FileReader reader = new FileReader(backupFile)) {
            BufferedReader br = new BufferedReader(reader);
            ToggleCollection toggleCollection = JsonToggleParser.fromJson(br);
            eventDispatcher.dispatch(new ToggleBackupRead(toggleCollection));
            return toggleCollection;
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
        List<FeatureToggle> emptyList = Collections.emptyList();
        return new ToggleCollection(emptyList);
    }

    @Override
    public void write(ToggleCollection toggleCollection) {
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(JsonToggleParser.toJsonString(toggleCollection));
            eventDispatcher.dispatch(new ToggleBackupWritten(toggleCollection));
        } catch (IOException e) {
            eventDispatcher.dispatch(
                    new UnleashException(
                            "Unleash was unable to backup feature toggles to file: " + backupFile,
                            e));
        }
    }

    private static class ToggleBackupRead implements UnleashEvent {

        private final ToggleCollection toggleCollection;

        private ToggleBackupRead(ToggleCollection toggleCollection) {
            this.toggleCollection = toggleCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.toggleBackupRestored(toggleCollection);
        }
    }

    private static class ToggleBackupWritten implements UnleashEvent {

        private final ToggleCollection toggleCollection;

        private ToggleBackupWritten(ToggleCollection toggleCollection) {
            this.toggleCollection = toggleCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.togglesBackedUp(toggleCollection);
        }
    }
}
