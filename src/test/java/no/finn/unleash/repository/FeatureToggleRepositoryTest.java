package no.finn.unleash.repository;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;


public class FeatureToggleRepositoryTest {
    private ToggleRepository toggleRepository;

    @Test
    public void noBackupFileAndNoRepositoryAvailable() {
        ToggleFetcher toggleFetcher = new HttpToggleFetcher(URI.create("http://localhost:4242/features"));
        ToggleBackupHandler toggleBackupHandler = new ToggleBackupHandlerFile();
        toggleRepository = new FeatureToggleRepository(toggleFetcher, toggleBackupHandler);
        assertNull("should be null", toggleRepository.getToggle("unknownFeature"));
    }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        new FeatureToggleRepository(toggleFetcher, toggleBackupHandler, Long.MAX_VALUE);

        verify(toggleBackupHandler, times(1)).read();
    }

}
