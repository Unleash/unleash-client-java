package no.finn.unleash.repository;

import no.finn.unleash.ActivationStrategy;
import no.finn.unleash.FeatureToggle;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class FeatureToggleRepositoryTest {

    @Test
    public void no_backup_file_and_no_repository_available_should_give_empty_repo() {
        ToggleFetcher toggleFetcher = new HttpToggleFetcher(URI.create("http://localhost:4242/features"));
        ToggleBackupHandler toggleBackupHandler = new ToggleBackupHandlerFile();
        ToggleRepository toggleRepository = new FeatureToggleRepository(toggleFetcher, toggleBackupHandler);
        assertNull("should be null", toggleRepository.getToggle("unknownFeature"));
    }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        new FeatureToggleRepository(toggleFetcher, toggleBackupHandler, Long.MAX_VALUE);

        verify(toggleBackupHandler, times(1)).read();
    }

    @Test
    public void feature_toggles_should_be_updated() throws URISyntaxException, InterruptedException {
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);

        ToggleCollection toggleCollection = populatedToggleCollection(new FeatureToggle("toggleFetcherCalled", false, new ArrayList<>()));
        when(toggleBackupHandler.read()).thenReturn(toggleCollection);

        toggleCollection = populatedToggleCollection(new FeatureToggle("toggleFetcherCalled", true, new ArrayList<>()));
        Response response = new Response(Response.Status.CHANGED, toggleCollection);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        ToggleRepository toggleRepository = new FeatureToggleRepository(toggleFetcher, toggleBackupHandler, 100L);
        assertFalse(toggleRepository.getToggle("toggleFetcherCalled").isEnabled());
        verify(toggleBackupHandler, times(1)).read();
        Thread.sleep(800L); //wait for background fetching
        verify(toggleFetcher, times(1)).fetchToggles();

        assertTrue(toggleRepository.getToggle("toggleFetcherCalled").isEnabled());
    }

    private ToggleCollection populatedToggleCollection(FeatureToggle featureToggle) {
        List<FeatureToggle> list = new ArrayList();
        list.add(featureToggle);
        return new ToggleCollection(list);

    }

}
