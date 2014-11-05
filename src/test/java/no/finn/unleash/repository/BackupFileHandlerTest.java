package no.finn.unleash.repository;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class BackupFileHandlerTest {

    @Test
    public void testRead() {
        BackupFileHandler backupFileHandler = new BackupFileHandler(getClass().getResource("/unleash-repo.json").getFile());
        ToggleCollection toggleCollection = backupFileHandler.read();
        assertNotNull("presentFeature should be present", toggleCollection.getToggle("presentFeature"));
    }

    @Test
    public void testReadWithoutFile() throws URISyntaxException {

        BackupFileHandler backupFileHandler = new BackupFileHandler("/does/not/exist.json");
        ToggleCollection toggleCollection = backupFileHandler.read();

        assertNull("presentFeature should not be present", toggleCollection.getToggle("presentFeature"));
    }

    @Test
    public void testWrite(){
        ToggleCollection toggleCollection = new ToggleCollection(JsonToggleParser.fromJson("{\"features\": [{\"name\": \"writableFeature\",\"enabled\": true,\"strategy\": \"default\"}]}"));
        String backupFile = System.getProperty("java.io.tmpdir") + File.separatorChar + "unleash-repo-test-write.json";
        BackupFileHandler backupFileHandler = new BackupFileHandler(backupFile);
        backupFileHandler.write(toggleCollection);
        backupFileHandler = new BackupFileHandler(backupFile);
        toggleCollection = backupFileHandler.read();
        assertNotNull("writableFeature should be present", toggleCollection.getToggle("writableFeature"));


    }

}