package io.getunleash.repository;

import com.google.gson.JsonParseException;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.util.UnleashConfig;
import java.io.*;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureBackupHandlerFile implements BackupHandler<FeatureCollection> {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureBackupHandlerFile.class);

    private final String backupFile;
    private final EventDispatcher eventDispatcher;

    public FeatureBackupHandlerFile(UnleashConfig config) {
        this.backupFile = config.getBackupFile();
        this.eventDispatcher = new EventDispatcher(config);
    }

    @Override
    public FeatureCollection read() {
        LOG.info("Unleash will try to load feature toggle states from temporary backup");
        try (FileReader reader = new FileReader(backupFile)) {
            BufferedReader br = new BufferedReader(reader);
            FeatureCollection featureCollection = JsonFeatureParser.fromJson(br);
            eventDispatcher.dispatch(new FeatureBackupRead(featureCollection));
            return featureCollection;
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
        return new FeatureCollection(
                new ToggleCollection(Collections.emptyList()),
                new SegmentCollection(Collections.emptyList()));
    }

    @Override
    public void write(FeatureCollection featureCollection) {
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(JsonFeatureParser.toJsonString(featureCollection));
            eventDispatcher.dispatch(new FeatureBackupWritten(featureCollection));
        } catch (IOException e) {
            eventDispatcher.dispatch(
                    new UnleashException(
                            "Unleash was unable to backup feature toggles to file: " + backupFile,
                            e));
        }
    }

    private static class FeatureBackupRead implements UnleashEvent {

        private final FeatureCollection featureCollection;

        private FeatureBackupRead(FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.featuresBackupRestored(featureCollection);
        }
    }

    private static class FeatureBackupWritten implements UnleashEvent {

        private final FeatureCollection featureCollection;

        private FeatureBackupWritten(FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.featuresBackedUp(featureCollection);
        }
    }
}
