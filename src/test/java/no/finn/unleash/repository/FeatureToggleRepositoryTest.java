package no.finn.unleash.repository;

import no.finn.unleash.ActivationStrategy;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


public class FeatureToggleRepositoryTest {

    @Test
    public void no_backup_file_and_no_repository_available_should_give_empty_repo() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("test")
                .unleashAPI("http://localhost:4242/").build();
        ToggleFetcher toggleFetcher = new HttpToggleFetcher(config);
        ToggleBackupHandler toggleBackupHandler = new ToggleBackupHandlerFile(config);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleRepository toggleRepository = new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);
        assertNull("should be null", toggleRepository.getToggle("unknownFeature"));
    }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        UnleashConfig config = UnleashConfig.builder()
                .appName("test")
                .unleashAPI("http://localhost:4242/")
                .fetchTogglesInterval(Long.MAX_VALUE)
                .build();

        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        verify(toggleBackupHandler, times(1)).read();
    }

    @Test
    public void feature_toggles_should_be_updated() throws URISyntaxException, InterruptedException {
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);

        //setup backupHandler
        ToggleBackupHandler toggleBackupHandler = mock(ToggleBackupHandler.class);
        ToggleCollection toggleCollection = populatedToggleCollection(
                new FeatureToggle("toggleFetcherCalled", false, Arrays.asList(new ActivationStrategy("custom", null))));
        when(toggleBackupHandler.read()).thenReturn(toggleCollection);

        //setup fetcher
        toggleCollection = populatedToggleCollection(
                new FeatureToggle("toggleFetcherCalled", true, Arrays.asList(new ActivationStrategy("custom", null))));
        FeatureToggleResponse response = new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        //init
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);


        UnleashConfig config = new UnleashConfig.Builder()
                .appName("test")
                .unleashAPI("http://localhost:4242")
                .fetchTogglesInterval(200l)
                .build();

        ToggleRepository toggleRepository = new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        //run the toggle fetcher callback
        verify(executor).setInterval(runnableArgumentCaptor.capture(), anyLong(), anyLong());
        verify(toggleFetcher, times(0)).fetchToggles();
        runnableArgumentCaptor.getValue().run();

        verify(toggleBackupHandler, times(1)).read();
        verify(toggleFetcher, times(1)).fetchToggles();
        assertTrue(toggleRepository.getToggle("toggleFetcherCalled").isEnabled());
    }

    private ToggleCollection populatedToggleCollection(FeatureToggle featureToggle) {
        List<FeatureToggle> list = new ArrayList();
        list.add(featureToggle);
        return new ToggleCollection(list);

    }

}
