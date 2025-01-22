package io.getunleash.repository;

import static io.getunleash.repository.FeatureToggleResponse.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.getunleash.*;
import io.getunleash.engine.UnleashEngine;
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
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

    // @Test
    // public void
    // no_backup_file_and_no_repository_available_should_give_empty_repo() {
    // UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
    // UnleashConfig config =
    // UnleashConfig.builder()
    // .appName("test")
    // .unleashAPI("http://localhost:4242/api/")
    // .scheduledExecutor(executor)
    // .build();

    // when(backupHandler.read()).thenReturn(new FeatureCollection());
    // when(bootstrapHandler.read()).thenReturn(new FeatureCollection());
    // FeatureRepository featureRepository = new FeatureRepository(config);
    // assertNull(featureRepository.getToggle("unknownFeature"), "should be null");
    // }

    @Test
    public void backup_toggles_should_be_loaded_at_startup() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashEngine engine = mock(UnleashEngine.class);
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

        new FeatureRepositoryImpl(
                config,
                backupHandler,
                engine,
                fetcher,
                bootstrapHandler,
                new EventDispatcher(config));

        verify(backupHandler, times(1)).read();
    }

    // @Test
    // public void feature_toggles_should_be_updated() {
    // UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
    // ArgumentCaptor<Runnable> runnableArgumentCaptor =
    // ArgumentCaptor.forClass(Runnable.class);

    // UnleashConfig config =
    // new UnleashConfig.Builder()
    // .appName("test")
    // .unleashAPI("http://localhost:4242/api/")
    // .scheduledExecutor(executor)
    // .fetchTogglesInterval(200L)
    // .synchronousFetchOnInitialisation(false)
    // .build();

    // when(backupHandler.read()).thenReturn(simpleFeatureCollection(false));

    // FeatureRepository featureRepository =
    // new FeatureRepository(config, backupHandler, executor, fetcher,
    // bootstrapHandler);
    // // run the toggleName fetcher callback
    // verify(executor).setInterval(runnableArgumentCaptor.capture(), anyLong(),
    // anyLong());
    // verify(fetcher, times(0)).fetchFeatures();

    // ClientFeaturesResponse response =
    // new ClientFeaturesResponse(CHANGED, simpleFeatureCollection(true));
    // when(fetcher.fetchFeatures()).thenReturn(response);
    // runnableArgumentCaptor.getValue().run();

    // verify(backupHandler, times(1)).read();
    // verify(fetcher, times(1)).fetchFeatures();
    // assertTrue(featureRepository.getToggle("toggleFetcherCalled").isEnabled());
    // }

    @Test
    public void should_perform_synchronous_fetch_on_initialisation() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashEngine engine = mock(UnleashEngine.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(true)
                        .scheduledExecutor(executor)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .build();

        when(backupHandler.read()).thenReturn(new FeatureCollection());

        FeatureCollection featureCollection = populatedFeatureCollection(null);
        ClientFeaturesResponse response = new ClientFeaturesResponse(CHANGED, featureCollection);
        when(fetcher.fetchFeatures()).thenReturn(response);

        new FeatureRepositoryImpl(
                config,
                backupHandler,
                engine,
                fetcher,
                bootstrapHandler,
                new EventDispatcher(config));

        verify(fetcher, times(1)).fetchFeatures();
    }

    @Test
    public void should_not_perform_synchronous_fetch_on_initialisation() {
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        UnleashEngine engine = mock(UnleashEngine.class);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .scheduledExecutor(executor)
                        .appName("test-sync-update")
                        .unleashAPI("http://localhost:8080")
                        .build();

        when(backupHandler.read()).thenReturn(new FeatureCollection());

        FeatureCollection featureCollection = populatedFeatureCollection(null);
        ClientFeaturesResponse response = new ClientFeaturesResponse(CHANGED, featureCollection);

        when(fetcher.fetchFeatures()).thenReturn(response);

        FeatureRepositoryImpl featureRepository = new FeatureRepositoryImpl(config, engine);

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
        UnleashEngine engine = mock(UnleashEngine.class);

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

        FeatureRepositoryImpl repo = new FeatureRepositoryImpl(config, engine);
        // we need to register a consumer and use that to assert that the feature
        // toggles are correct
        repo.addConsumer(
                (fc) -> {
                    assertThat(fc.getMessage().getFeatures()).hasSize(5);
                });
    }

    @Test
    public void should_not_read_bootstrap_if_backup_was_found()
            throws IOException, URISyntaxException {
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v2.json").toURI());
        UnleashEngine engine = mock(UnleashEngine.class);
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

        when(backupHandler.read()).thenReturn(getFeatureCollection());

        new FeatureRepositoryImpl(
                config,
                backupHandler,
                engine,
                fetcher,
                bootstrapHandler,
                new EventDispatcher(config));
        verify(toggleBootstrapProvider, times(0)).read();
    }

    @Test
    public void shouldCallStartupExceptionHandlerIfStartupFails() {
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        UnleashScheduledExecutor executor = mock(UnleashScheduledExecutor.class);
        AtomicBoolean failed = new AtomicBoolean(false);
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(true)
                        .startupExceptionHandler(
                                (e) -> {
                                    failed.set(true);
                                })
                        .appName("test-sync-update")
                        .scheduledExecutor(executor)
                        .unleashAPI("http://localhost:8080")
                        .toggleBootstrapProvider(toggleBootstrapProvider)
                        .build();

        Unleash unleash = new DefaultUnleash(config);
        assertThat(failed).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 404})
    public void should_increase_to_max_interval_when_code(int code)
            throws URISyntaxException, IOException {
        TestRunner runner = new TestRunner();
        UnleashEngine engine = mock(UnleashEngine.class);
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v2.json").toURI());
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        when(toggleBootstrapProvider.read()).thenReturn(fileToString(file));
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .scheduledExecutor(runner.executor)
                        .unleashAPI("http://localhost:8080")
                        .build();
        when(backupHandler.read()).thenReturn(getFeatureCollection());

        FeatureRepositoryImpl featureRepository =
                new FeatureRepositoryImpl(
                        config,
                        backupHandler,
                        engine,
                        fetcher,
                        bootstrapHandler,
                        new EventDispatcher(config));
        runner.assertThatFetchesAndReceives(CHANGED, 200); // set it ready

        runner.assertThatFetchesAndReceives(UNAVAILABLE, code);
        assertThat(featureRepository.getFailures()).isEqualTo(1);
        assertThat(featureRepository.getSkips()).isEqualTo(30);
        for (int i = 0; i < 30; i++) {
            runner.assertThatSkipsNextRun();
        }
        assertThat(featureRepository.getFailures()).isEqualTo(1);
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getFailures()).isEqualTo(0);
        assertThat(featureRepository.getSkips()).isEqualTo(0);
    }

    @Test
    public void should_incrementally_increase_interval_as_we_receive_too_many_requests()
            throws URISyntaxException, IOException {
        TestRunner runner = new TestRunner();
        UnleashEngine engine = mock(UnleashEngine.class);
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v2.json").toURI());
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        when(toggleBootstrapProvider.read()).thenReturn(fileToString(file));
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .scheduledExecutor(runner.executor)
                        .unleashAPI("http://localhost:8080")
                        .build();
        when(backupHandler.read()).thenReturn(getFeatureCollection());
        FeatureRepositoryImpl featureRepository =
                new FeatureRepositoryImpl(
                        config,
                        backupHandler,
                        engine,
                        fetcher,
                        bootstrapHandler,
                        new EventDispatcher(config));

        runner.assertThatFetchesAndReceives(UNAVAILABLE, 429);
        // client is not ready don't count errors or skips
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(0);

        runner.assertThatFetchesAndReceives(UNAVAILABLE, 429);
        // client is not ready don't count errors or skips
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(0);

        // this changes the client to ready
        runner.assertThatFetchesAndReceives(CHANGED, 200);
        assertThat(featureRepository.getSkips()).isEqualTo(0);

        runner.assertThatFetchesAndReceives(UNAVAILABLE, 429);
        assertThat(featureRepository.getSkips()).isEqualTo(1);
        assertThat(featureRepository.getFailures()).isEqualTo(1);

        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(1);

        runner.assertThatFetchesAndReceives(UNAVAILABLE, 429);
        assertThat(featureRepository.getSkips()).isEqualTo(2);
        assertThat(featureRepository.getFailures()).isEqualTo(2);

        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(1);
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(2);

        runner.assertThatFetchesAndReceives(UNAVAILABLE, 429);
        assertThat(featureRepository.getSkips()).isEqualTo(3);
        assertThat(featureRepository.getFailures()).isEqualTo(3);

        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(3);

        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getSkips()).isEqualTo(2);
        assertThat(featureRepository.getFailures()).isEqualTo(2);

        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(2);

        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getSkips()).isEqualTo(1);
        assertThat(featureRepository.getFailures()).isEqualTo(1);

        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(1);

        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(0);
    }

    @Test
    public void server_errors_should_incrementally_increase_interval()
            throws URISyntaxException, IOException {
        TestRunner runner = new TestRunner();
        UnleashEngine engine = mock(UnleashEngine.class);
        File file =
                new File(getClass().getClassLoader().getResource("unleash-repo-v2.json").toURI());
        ToggleBootstrapProvider toggleBootstrapProvider = mock(ToggleBootstrapProvider.class);
        when(toggleBootstrapProvider.read()).thenReturn(fileToString(file));
        UnleashConfig config =
                UnleashConfig.builder()
                        .synchronousFetchOnInitialisation(false)
                        .appName("test-sync-update")
                        .scheduledExecutor(runner.executor)
                        .unleashAPI("http://localhost:8080")
                        .build();
        when(backupHandler.read()).thenReturn(getFeatureCollection());

        FeatureRepositoryImpl featureRepository =
                new FeatureRepositoryImpl(
                        config,
                        backupHandler,
                        engine,
                        fetcher,
                        bootstrapHandler,
                        new EventDispatcher(config));

        runner.assertThatFetchesAndReceives(CHANGED, 200); // set it ready

        runner.assertThatFetchesAndReceives(UNAVAILABLE, 500);
        assertThat(featureRepository.getSkips()).isEqualTo(1);
        assertThat(featureRepository.getFailures()).isEqualTo(1);
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(1);
        runner.assertThatFetchesAndReceives(UNAVAILABLE, 502);
        assertThat(featureRepository.getSkips()).isEqualTo(2);
        assertThat(featureRepository.getFailures()).isEqualTo(2);
        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(2);
        runner.assertThatFetchesAndReceives(UNAVAILABLE, 503);
        assertThat(featureRepository.getSkips()).isEqualTo(3);
        assertThat(featureRepository.getFailures()).isEqualTo(3);
        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(3);
        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getSkips()).isEqualTo(2);
        assertThat(featureRepository.getFailures()).isEqualTo(2);
        runner.assertThatSkipsNextRun();
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(2);
        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getSkips()).isEqualTo(1);
        assertThat(featureRepository.getFailures()).isEqualTo(1);
        runner.assertThatSkipsNextRun();
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(1);
        runner.assertThatFetchesAndReceives(NOT_CHANGED, 304);
        assertThat(featureRepository.getSkips()).isEqualTo(0);
        assertThat(featureRepository.getFailures()).isEqualTo(0);
    }

    private String fileToString(File f) throws IOException {
        return new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
    }

    @NotNull
    private FeatureCollection simpleFeatureCollection(boolean enabled) {
        return populatedFeatureCollection(
                null,
                new FeatureToggle(
                        "toggleFetcherCalled",
                        enabled,
                        Arrays.asList(new ActivationStrategy("custom", null))));
    }

    @NotNull
    private FeatureCollection getFeatureCollection() {
        return populatedFeatureCollection(
                Arrays.asList(
                        new Segment(
                                1,
                                "some-name",
                                Arrays.asList(
                                        new Constraint(
                                                "some-context", Operator.IN, "some-value")))),
                new FeatureToggle(
                        "toggleFeatureName1",
                        true,
                        Collections.singletonList(new ActivationStrategy("custom", null))),
                new FeatureToggle(
                        "toggleFeatureName2",
                        true,
                        Collections.singletonList(new ActivationStrategy("custom", null))));
    }

    private class TestRunner {

        private final UnleashScheduledExecutor executor;
        private final ArgumentCaptor<Runnable> runnableArgumentCaptor;
        private int count = 0;

        private boolean initialized = false;

        public TestRunner() {
            this.executor = mock(UnleashScheduledExecutor.class);
            this.runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        }

        private void ensureInitialized() {
            if (!initialized) {
                verify(executor)
                        .setInterval(runnableArgumentCaptor.capture(), anyLong(), anyLong());
                initialized = true;
            }
        }

        public void assertThatFetchesAndReceives(
                FeatureToggleResponse.Status status, int statusCode) {
            ensureInitialized();
            when(fetcher.fetchFeatures())
                    .thenReturn(new ClientFeaturesResponse(status, statusCode));
            runnableArgumentCaptor.getValue().run();
            verify(fetcher, times(++count)).fetchFeatures();
        }

        public void assertThatSkipsNextRun() {
            ensureInitialized();
            runnableArgumentCaptor.getValue().run();
            verify(fetcher, times(count)).fetchFeatures();
        }
    }
}
