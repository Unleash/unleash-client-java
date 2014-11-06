package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class ToggleBackupHandlerFile implements ToggleBackupHandler {
    private static final Logger LOG = LogManager.getLogger();
    private final String backupFile;

    public ToggleBackupHandlerFile(){
        this(System.getProperty("java.io.tmpdir") + File.separatorChar + "unleash-repo.json");
    }

    public ToggleBackupHandlerFile(String backupFile){
        this.backupFile = backupFile;
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
        } catch (IOException e) {
            LOG.error("Failed to read backup file:'{}'", backupFile, e);
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
