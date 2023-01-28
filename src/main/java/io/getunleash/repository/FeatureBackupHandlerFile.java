package io.getunleash.repository;

import com.google.gson.JsonParseException;
import io.getunleash.UnleashException;
import io.getunleash.util.UnleashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;

public class FeatureBackupHandlerFile extends AbstractBackupHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FeatureBackupHandlerFile.class);

    private final String backupFile;

    public FeatureBackupHandlerFile(final UnleashConfig config) {
        super(config);
        this.backupFile = config.getBackupFile();
    }

    @Override
    protected final FeatureCollection readFeatureCollection() {
        LOG.info("Unleash will try to load feature toggle states from temporary backup");
        try (final FileReader reader = new FileReader(backupFile)) {
            final BufferedReader br = new BufferedReader(reader);

            return JsonFeatureParser.fromJson(br);
        } catch (final FileNotFoundException e) {
            LOG.info(
                " Unleash could not find the backup-file '"
                    + backupFile
                    + "'. \n"
                    + "This is expected behavior the first time unleash runs in a new environment.");
        } catch (final IOException | IllegalStateException | JsonParseException e) {
            throw new UnleashException("Failed to read backup file: " + backupFile, e);
        }

        return new FeatureCollection(
            new ToggleCollection(Collections.emptyList()),
            new SegmentCollection(Collections.emptyList()));
    }

    @Override
    protected final void writeFeatureCollection(final FeatureCollection featureCollection) {
        try (final FileWriter writer = new FileWriter(backupFile)) {
            writer.write(JsonFeatureParser.toJsonString(featureCollection));
        } catch (IOException e) {
            throw
                new UnleashException(
                    "Unleash was unable to backup feature toggles to file: " + backupFile,
                    e);
        }
    }
}
