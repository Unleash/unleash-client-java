package io.getunleash.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.getunleash.ActivationStrategy;
import io.getunleash.FeatureToggle;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class FeatureToggleRepositoryTest {

    @Test
    public void no_backup_file_and_no_repository_available_should_give_empty_repo() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .build();
        ToggleFetcher toggleFetcher = new HttpToggleFetcher(config);
        BackupHandler<ToggleCollection> toggleBackupHandler = new ToggleBackupHandlerFile(config);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleRepository toggleRepository =
                new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);
        assertNull(toggleRepository.getToggle("unknownFeature"), "should be null");
    }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .fetchTogglesInterval(Long.MAX_VALUE)
                        .build();

        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        when(toggleBackupHandler.read()).thenReturn(new ToggleCollection(Collections.emptyList()));
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        verify(toggleBackupHandler, times(1)).read();
    }

    @Test
    public void feature_toggles_should_be_updated()
            throws URISyntaxException, InterruptedException {
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);

        // setup backupHandler
        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        ToggleCollection toggleCollection =
                populatedToggleCollection(
                        new FeatureToggle(
                                "toggleFetcherCalled",
                                false,
                                Arrays.asList(new ActivationStrategy("custom", null))));
        when(toggleBackupHandler.read()).thenReturn(toggleCollection);

        // setup fetcher
        toggleCollection =
                populatedToggleCollection(
                        new FeatureToggle(
                                "toggleFetcherCalled",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null))));
        FeatureToggleResponse response =
                new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        // init
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);

        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .fetchTogglesInterval(200L)
                        .build();

        ToggleRepository toggleRepository =
                new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        // run the toggleName fetcher callback
        verify(executor).setInterval(runnableArgumentCaptor.capture(), anyLong(), anyLong());
        verify(toggleFetcher, times(0)).fetchToggles();
        runnableArgumentCaptor.getValue().run();

        verify(toggleBackupHandler, times(1)).read();
        verify(toggleFetcher, times(1)).fetchToggles();
        assertTrue(toggleRepository.getToggle("toggleFetcherCalled").isEnabled());
    }

    @Test
    public void get_feature_names_should_return_list_of_names() {
        UnleashConfig config = mock(UnleashConfig.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);

        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);

        ToggleCollection toggleCollection =
                populatedToggleCollection(
                        new FeatureToggle(
                                "toggleFeatureName1",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null))),
                        new FeatureToggle(
                                "toggleFeatureName2",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null))));
        when(toggleBackupHandler.read()).thenReturn(toggleCollection);

        ToggleRepository toggleRepository =
                new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        assertEquals(2, toggleRepository.getFeatureNames().size());
        assertEquals("toggleFeatureName2", toggleRepository.getFeatureNames().get(1));
    }

    @Test
    public void should_perform_synchronous_fetch_on_initialisation() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(true)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        when(toggleBackupHandler.read()).thenReturn(new ToggleCollection(Collections.emptyList()));

        // setup fetcher
        ToggleCollection toggleCollection = populatedToggleCollection();
        FeatureToggleResponse response =
                new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        verify(toggleFetcher, times(1)).fetchToggles();
    }

    @Test
    public void should_perform_synchronous_fetch_on_initialisation_and_fail_if_invalid_upstream() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(true)
                        .appName("test-sync-update")
                        .unleashAPI("http://wrong-host:8383")
                        .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        when(toggleBackupHandler.read()).thenReturn(new ToggleCollection(Collections.emptyList()));

        // setup fetcher
        when(toggleFetcher.fetchToggles()).thenThrow(RuntimeException.class);

        assertThatThrownBy(
                        () -> {
                            new FeatureToggleRepository(
                                    config, executor, toggleFetcher, toggleBackupHandler);
                        })
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_not_perform_synchronous_fetch_on_initialisation() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        when(toggleBackupHandler.read()).thenReturn(new ToggleCollection(Collections.emptyList()));

        // setup fetcher
        ToggleCollection toggleCollection = populatedToggleCollection();
        FeatureToggleResponse response =
                new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggleCollection);
        when(toggleFetcher.fetchToggles()).thenReturn(response);

        new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);

        verify(toggleFetcher, times(0)).fetchToggles();
    }

    private ToggleCollection populatedToggleCollection(FeatureToggle... featureToggles) {
        List<FeatureToggle> list = new ArrayList<>(Arrays.asList(featureToggles));
        return new ToggleCollection(list);
    }

    @Test
    public void should_read_from_bootstrap_location_if_backup_was_empty()
            throws URISyntaxException, IOException {
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v1.json").toURI());
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        when(toggleBootstrapProvider.read()).thenReturn(fileToString(file));
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .toggleBootstrapProvider(toggleBootstrapProvider)
                        .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        when(toggleBackupHandler.read()).thenReturn(new ToggleCollection(Collections.emptyList()));
        FeatureToggleRepository repo =
                new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);
        assertThat(repo.getFeatureNames()).hasSize(3);
    }

    @Test
    public void should_not_read_bootstrap_if_backup_was_found()
            throws IOException, URISyntaxException {
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v1.json").toURI());
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        when(toggleBootstrapProvider.read()).thenReturn(fileToString(file));
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .toggleBootstrapProvider(toggleBootstrapProvider)
                        .build();
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleFetcher toggleFetcher = mock(ToggleFetcher.class);
        BackupHandler<ToggleCollection> toggleBackupHandler = mock(BackupHandler.class);
        when(toggleBackupHandler.read())
                .thenReturn(
                        populatedToggleCollection(
                                new FeatureToggle(
                                        "toggleFeatureName1",
                                        true,
                                        Collections.singletonList(
                                                new ActivationStrategy("custom", null))),
                                new FeatureToggle(
                                        "toggleFeatureName2",
                                        true,
                                        Collections.singletonList(
                                                new ActivationStrategy("custom", null)))));
        FeatureToggleRepository repo =
                new FeatureToggleRepository(config, executor, toggleFetcher, toggleBackupHandler);
        verify(toggleBootstrapProvider, times(0)).read();
    }

    private String fileToString(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
}
