package io.getunleash.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.getunleash.*;
import io.getunleash.event.EventDispatcher;
import io.getunleash.lang.Nullable;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class FeatureRepositoryTest {

    FeatureBackupHandlerFile backupHandler;

    FeatureBootstrapHandler bootstrapHandler;
    HttpFeatureFetcher fetcher;

    UnleashConfig defaultConfig;

    @BeforeEach
    public void setUp() {
        backupHandler = mock(FeatureBackupHandlerFile.class);
        bootstrapHandler = mock(FeatureBootstrapHandler.class);
        fetcher = mock(HttpFeatureFetcher.class);

        defaultConfig =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .fetchTogglesInterval(200L)
                        .synchronousFetchOnInitialisation(false)
                        .build();
    }

    @Test
    public void no_backup_file_and_no_repository_available_should_give_empty_repo() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .scheduledExecutor(executor)
                        .build();

        when(backupHandler.read()).thenReturn(new FeatureCollection());
        when(bootstrapHandler.read()).thenReturn(new FeatureCollection());
        FeatureRepository featureRepository = new FeatureRepository(config);
        assertNull(featureRepository.getToggle("unknownFeature"), "should be null");
    }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .scheduledExecutor(executor)
                        .unleashAPI("http://localhost:4242/api/")
                        .fetchTogglesInterval(Long.MAX_VALUE)
                        .build();

        when(backupHandler.read())
                .thenReturn(
                        new FeatureCollection(
                                new ToggleCollection(Collections.emptyList()),
                                new SegmentCollection(Collections.emptyList())));

        new FeatureRepository(
                config, backupHandler, new EventDispatcher(config), fetcher, bootstrapHandler);

        verify(backupHandler, times(1)).read();
    }

    @Test
    public void feature_toggles_should_be_updated() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);

        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .scheduledExecutor(executor)
                        .fetchTogglesInterval(200L)
                        .synchronousFetchOnInitialisation(false)
                        .build();

        FeatureCollection featureCollection =
                populatedFeatureCollection(
                        null,
                        new FeatureToggle(
                                "toggleFetcherCalled",
                                false,
                                Arrays.asList(new ActivationStrategy("custom", null, null))));

        when(backupHandler.read()).thenReturn(featureCollection);

        featureCollection =
                populatedFeatureCollection(
                        null,
                        new FeatureToggle(
                                "toggleFetcherCalled",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null, null))));
        ClientFeaturesResponse response =
                new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED, featureCollection);

        FeatureRepository featureRepository =
                new FeatureRepository(config, backupHandler, executor, fetcher, bootstrapHandler);
        // run the toggleName fetcher callback
        verify(executor).setInterval(runnableArgumentCaptor.capture(), anyLong(), anyLong());
        verify(fetcher, times(0)).fetchFeatures();

        when(fetcher.fetchFeatures()).thenReturn(response);
        runnableArgumentCaptor.getValue().run();

        verify(backupHandler, times(1)).read();
        verify(fetcher, times(1)).fetchFeatures();
        assertTrue(featureRepository.getToggle("toggleFetcherCalled").isEnabled());
    }

    @Test
    public void get_feature_names_should_return_list_of_names() {
        FeatureCollection featureCollection =
                populatedFeatureCollection(
                        null,
                        new FeatureToggle(
                                "toggleFeatureName1",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null, null))),
                        new FeatureToggle(
                                "toggleFeatureName2",
                                true,
                                Arrays.asList(new ActivationStrategy("custom", null, null))));

        when(backupHandler.read()).thenReturn(featureCollection);
        FeatureRepository featureRepository =
                new FeatureRepository(
                        defaultConfig,
                        backupHandler,
                        new EventDispatcher(defaultConfig),
                        fetcher,
                        bootstrapHandler);
        assertEquals(2, featureRepository.getFeatureNames().size());
        assertEquals("toggleFeatureName2", featureRepository.getFeatureNames().get(1));
    }

    @Test
    public void should_perform_synchronous_fetch_on_initialisation() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(true)
                        .scheduledExecutor(executor)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .build();

        when(backupHandler.read()).thenReturn(new FeatureCollection());

        FeatureCollection featureCollection = populatedFeatureCollection(null);
        ClientFeaturesResponse response =
                new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED, featureCollection);
        when(fetcher.fetchFeatures()).thenReturn(response);

        new FeatureRepository(
                config, backupHandler, new EventDispatcher(config), fetcher, bootstrapHandler);
        verify(fetcher, times(1)).fetchFeatures();
    }

    @Test
    public void should_not_perform_synchronous_fetch_on_initialisation() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .scheduledExecutor(executor)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .build();

        when(backupHandler.read()).thenReturn(new FeatureCollection());

        FeatureCollection featureCollection = populatedFeatureCollection(null);
        ClientFeaturesResponse response =
                new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED, featureCollection);

        when(fetcher.fetchFeatures()).thenReturn(response);

        FeatureRepository featureRepository = new FeatureRepository(config);

        verify(fetcher, times(0)).fetchFeatures();
    }

    private FeatureCollection populatedFeatureCollection(
            @Nullable List<Segment> segments, FeatureToggle... featureToggles) {
        List<FeatureToggle> toggleList = new ArrayList<>();
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

        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .scheduledExecutor(executor)
                        .unleashAPI("http://localhost:8080")
                        .toggleBootstrapProvider(toggleBootstrapProvider)
                        .build();

        when(backupHandler.read()).thenReturn(new FeatureCollection());

        FeatureRepository repo = new FeatureRepository(config);
        assertThat(repo.getFeatureNames()).hasSize(5);
    }

    @Test
    public void should_not_read_bootstrap_if_backup_was_found()
            throws IOException, URISyntaxException {
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v2.json").toURI());
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .scheduledExecutor(executor)
                        .unleashAPI("http://localhost:8080")
                        .toggleBootstrapProvider(toggleBootstrapProvider)
                        .build();

        when(toggleBootstrapProvider.read()).thenReturn(fileToString(file));

        when(backupHandler.read())
                .thenReturn(
                        populatedFeatureCollection(
                                Arrays.asList(
                                        new Segment(
                                                1,
                                                "some-name",
                                                Arrays.asList(
                                                        new Constraint(
                                                                "some-context",
                                                                Operator.IN,
                                                                "some-value")))),
                                new FeatureToggle(
                                        "toggleFeatureName1",
                                        true,
                                        Collections.singletonList(
                                                new ActivationStrategy("custom", null, null))),
                                new FeatureToggle(
                                        "toggleFeatureName2",
                                        true,
                                        Collections.singletonList(
                                                new ActivationStrategy("custom", null, null)))));

        new FeatureRepository(
                config, backupHandler, new EventDispatcher(config), fetcher, bootstrapHandler);
        verify(toggleBootstrapProvider, times(0)).read();
    }

    private String fileToString(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }
}
