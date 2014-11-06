package no.finn.unleash.repository;

import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class ToggleBackupHandlerFileTest {

    @Test
    public void testRead() {
        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(getClass().getResource("/unleash-repo.json").getFile());
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull("presentFeature should be present", toggleCollection.getToggle("presentFeature"));
    }

    @Test
    public void testReadWithoutFile() throws URISyntaxException {

        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile("/does/not/exist.json");
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();

        assertNull("presentFeature should not be present", toggleCollection.getToggle("presentFeature"));
    }

    @Test
    public void testWrite(){
        ToggleCollection toggleCollection = new ToggleCollection(JsonToggleParser.fromJson("{\"features\": [{\"name\": \"writableFeature\",\"enabled\": true,\"strategy\": \"default\"}]}"));
        String backupFile = System.getProperty("java.io.tmpdir") + File.separatorChar + "unleash-repo-test-write.json";
        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(backupFile);
        toggleBackupHandlerFile.write(toggleCollection);
        toggleBackupHandlerFile = new ToggleBackupHandlerFile(backupFile);
        toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull("writableFeature should be present", toggleCollection.getToggle("writableFeature"));


    }

}