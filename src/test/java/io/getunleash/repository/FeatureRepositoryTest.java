package io.getunleash.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.getunleash.*;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class FeatureRepositoryTest {

    @Test
    public void no_backup_file_and_no_repository_available_should_give_empty_repo() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .build();
        FeatureFetcher fetcher = new HttpFeatureFetcher(config);
        BackupHandler<FeatureCollection> featureCollectionBackupHandler =
                new FeatureBackupHandlerFile(config);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ToggleRepository toggleRepository =
                new FeatureRepository(config, executor, fetcher, featureCollectionBackupHandler);
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

        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);
        when(backupHandler.read())
                .thenReturn(
                        new FeatureCollection(
                                new ToggleCollection(Collections.emptyList()),
                                new SegmentCollection(Collections.emptyList())));
        FeatureFetcher fetcher = mock(FeatureFetcher.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        new FeatureRepository(config, executor, fetcher, backupHandler);

        verify(backupHandler, times(1)).read();
    }

    @Test
    public void feature_toggles_should_be_updated()
            throws URISyntaxException, InterruptedException {
        FeatureFetcher fetcher = mock(FeatureFetcher.class);

        // setup backupHandler
        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);
        FeatureCollection featureCollection =
                populatedFeatureCollection(
                        null,
                        new FeatureToggle(
                                "toggleFetcherCalled",
                                false,
                                Arrays.asList(new ActivationStrategy("custom", null))));
        when(backupHandler.read()).thenReturn(featureCollection);

        // setup fetcher
        featureCollection =
                populatedFeatureCollection(
                        null,
                        new FeatureToggle(
                                "toggleFetcherCalled",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null))));
        ClientFeaturesResponse response =
                new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED, featureCollection);

        when(fetcher.fetchFeatures()).thenReturn(response);

        // init
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);

        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .fetchTogglesInterval(200L)
                        .build();

        FeatureRepository featureRepository =
                new FeatureRepository(config, executor, fetcher, backupHandler);

        // run the toggleName fetcher callback
        verify(executor).setInterval(runnableArgumentCaptor.capture(), anyLong(), anyLong());
        verify(fetcher, times(0)).fetchFeatures();
        runnableArgumentCaptor.getValue().run();

        verify(backupHandler, times(1)).read();
        verify(fetcher, times(1)).fetchFeatures();
        assertTrue(featureRepository.getToggle("toggleFetcherCalled").isEnabled());
    }

    @Test
    public void get_feature_names_should_return_list_of_names() {
        UnleashConfig config = mock(UnleashConfig.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        FeatureFetcher fetcher = mock(FeatureFetcher.class);

        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);

        FeatureCollection featureCollection =
                populatedFeatureCollection(
                        null,
                        new FeatureToggle(
                                "toggleFeatureName1",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null))),
                        new FeatureToggle(
                                "toggleFeatureName2",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null))));
        when(backupHandler.read()).thenReturn(featureCollection);

        FeatureRepository featureRepository =
            new FeatureRepository(config, executor, fetcher, backupHandler);

        assertEquals(2, featureRepository.getFeatureNames().size());
        assertEquals("toggleFeatureName2", featureRepository.getFeatureNames().get(1));
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

        FeatureFetcher fetcher = mock(HttpFeatureFetcher.class);
        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);
        when(backupHandler.read()).thenReturn(new FeatureCollection());

        // setup fetcher
        FeatureCollection featureCollection = populatedFeatureCollection(null);
        ClientFeaturesResponse response =
                new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED, featureCollection);
        when(fetcher.fetchFeatures()).thenReturn(response);

        new FeatureRepository(config, executor, fetcher, backupHandler);

        verify(fetcher, times(1)).fetchFeatures();
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
        FeatureFetcher fetcher = mock(HttpFeatureFetcher.class);
        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);
        when(backupHandler.read()).thenReturn(new FeatureCollection());

        // setup fetcher
        FeatureCollection featureCollection = populatedFeatureCollection(null);
        ClientFeaturesResponse response =
                new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED, featureCollection);
        when(fetcher.fetchFeatures()).thenReturn(response);

        new FeatureRepository(config, executor, fetcher, backupHandler);

        verify(fetcher, times(0)).fetchFeatures();
    }

    private FeatureCollection populatedFeatureCollection(
            @Nullable List<Segment> segments, FeatureToggle... featureToggles) {
        List<FeatureToggle> toggleList = new ArrayList();
        toggleList.addAll(Arrays.asList(featureToggles));

        List<Segment> segmentList = new ArrayList<>();
        if (segments != null) segmentList.addAll(segments);

        return new FeatureCollection(
                new ToggleCollection(toggleList), new SegmentCollection(segmentList));
    }

    @Test
    public void should_read_from_bootstrap_location_if_backup_was_empty()
            throws URISyntaxException, IOException {
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v2.json").toURI());
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
        FeatureFetcher fetcher = new HttpFeatureFetcher(config);
        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);
        when(backupHandler.read()).thenReturn(new FeatureCollection());
        FeatureRepository repo = new FeatureRepository(config, executor, fetcher, backupHandler);
        assertThat(repo.getFeatureNames()).hasSize(5);
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
        FeatureFetcher fetcher = new HttpFeatureFetcher(config);
        BackupHandler<FeatureCollection> backupHandler = mock(FeatureBackupHandlerFile.class);
        when(backupHandler.read())
                .thenReturn(
                        populatedFeatureCollection(
                                Arrays.asList(
                                        new Segment(
                                                1,
                                                "some-name",
                                                null,
                                                Arrays.asList(
                                                        new Constraint(
                                                                "some-context",
                                                                Operator.IN,
                                                                "some-value")),
                                                "some-created-by",
                                                "some-created-on")),
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
        FeatureRepository repo = new FeatureRepository(config, executor, fetcher, backupHandler);
        verify(toggleBootstrapProvider, times(0)).read();
    }

    private String fileToString(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
}
