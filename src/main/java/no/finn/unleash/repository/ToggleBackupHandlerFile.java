package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;
import no.finn.unleash.util.UnleashConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collections;
import java.util.List;
import com.google.gson.JsonParseException;

public class ToggleBackupHandlerFile implements ToggleBackupHandler {
    private static final Logger LOG = LogManager.getLogger(ToggleBackupHandlerFile.class);
    private final String backupFile;

    public ToggleBackupHandlerFile(UnleashConfig config){
        this.backupFile = config.getBackupFile();
    }

    @Override
    public ToggleCollection read() {
        LOG.info("Unleash will try to load feature toggle states from temporary backup");
        try (FileReader reader = new FileReader(backupFile)) {
            BufferedReader br = new BufferedReader(reader);
            return JsonToggleParser.fromJson(br);
        } catch (FileNotFoundException e) {
            LOG.warn(" Unleash could not find the backup-file '" + backupFile + "'. \n" +
                    "This is expected behavior the first time unleash runs in a new environment.");
        } catch (IOException | IllegalStateException | JsonParseException e) {
            LOG.warn("Failed to read backup file:'{}'", backupFile, e);
        }
        List<FeatureToggle> emptyList = Collections.emptyList();
        return new ToggleCollection(emptyList);
    }

    @Override
    public void write(ToggleCollection toggleCollection) {
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(JsonToggleParser.toJsonString(toggleCollection));
        } catch (IOException e) {
            LOG.warn("Unleash was unable to backup feature toggles to file: {}", backupFile, e);
        }
    }
}
