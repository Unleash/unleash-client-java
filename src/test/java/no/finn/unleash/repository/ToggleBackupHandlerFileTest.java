package no.finn.unleash.repository;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class ToggleBackupHandlerFileTest {

    @Test
    public void test_read() {
        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(getClass().getResource("/unleash-repo.json").getFile());
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull("presentFeature should be present", toggleCollection.getToggle("presentFeature"));
    }

    @Test
    public void test_read_file_with_invalid_data() throws Exception {
        ToggleBackupHandlerFile fileGivingNullFeature = new ToggleBackupHandlerFile(getClass().getResource("/unleash-repo-without-feature-field.json").getFile());
        assertNotNull(fileGivingNullFeature.read());
    }

    @Test
    public void test_read_without_file() throws URISyntaxException {

        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile("/does/not/exist.json");
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();

        assertNull("presentFeature should not be present", toggleCollection.getToggle("presentFeature"));
    }

    @Test
    public void test_write_strategies(){
        String staticData = "{\"features\": [{\"name\": \"writableFeature\",\"enabled\": true,\"strategy\": \"default\"}]}";
        Reader staticReader = new StringReader(staticData);
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(staticReader);
        String backupFile = System.getProperty("java.io.tmpdir") + File.separatorChar + "unleash-repo-test-write.json";
        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(backupFile);
        toggleBackupHandlerFile.write(toggleCollection);
        toggleBackupHandlerFile = new ToggleBackupHandlerFile(backupFile);
        toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull("writableFeature should be present", toggleCollection.getToggle("writableFeature"));
    }

    @Test
    public void test_read_old_format_with_strategies() {
        ToggleBackupHandlerFile toggleBackupHandlerFile = new ToggleBackupHandlerFile(getClass().getResource("/unleash-repo-v0.json").getFile());
        ToggleCollection toggleCollection = toggleBackupHandlerFile.read();
        assertNotNull("presentFeature should be present", toggleCollection.getToggle("featureCustomStrategy"));
        assertEquals("should have 1 strategy", toggleCollection.getToggle("featureCustomStrategy").getStrategies().size(), 1);
        assertEquals(toggleCollection.getToggle("featureCustomStrategy").getStrategies().get(0).getParameters().get("customParameter"), "customValue");
    }

}