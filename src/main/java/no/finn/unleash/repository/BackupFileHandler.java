package no.finn.unleash.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Collections;

class BackupFileHandler {
    private static final Logger LOG = LogManager.getLogger();
    private final String backupFile;

    BackupFileHandler(){
        this(System.getProperty("java.io.tmpdir") + File.separatorChar + "unleash-repo.json");
    }

    BackupFileHandler(String backupFile){
        this.backupFile = backupFile;
    }


    ToggleCollection read() {
        LOG.info("Unleash will try to load feature toggle states from temporary backup");
        try (FileReader reader = new FileReader(backupFile)) {
            BufferedReader br = new BufferedReader(reader);
            return JsonToggleParser.fromJson(br);
        } catch (FileNotFoundException e) {
            LOG.warn("Unable to locate backup file:'{}'", backupFile, e);
        } catch (IOException e) {
            LOG.error("Failed to read backup file:'{}'", backupFile, e);
        }
        return new ToggleCollection(Collections.EMPTY_LIST);
    }

    void write(ToggleCollection toggleCollection) {
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(JsonToggleParser.toJsonString(toggleCollection));
        } catch (IOException e) {
            LOG.warn("Unleash was unable to backup feature toggles to file: {}", backupFile, e);
        }
    }
}
