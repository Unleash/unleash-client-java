package io.getunleash;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.getunleash.event.EventDispatcher;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.strategy.DefaultStrategy;
import io.getunleash.strategy.Strategy;
import io.getunleash.util.UnleashConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class DefaultUnleashTest {
    private DefaultUnleash sut;
    private FeatureRepository featureRepository;
    private UnleashContextProvider contextProvider;
    private EventDispatcher eventDispatcher;
    private UnleashMetricService metricService;

    @BeforeEach
    public void setup() {
        UnleashConfig unleashConfig =
                UnleashConfig.builder().unleashAPI("http://fakeAPI").appName("fakeApp").build();
        featureRepository = mock(FeatureRepository.class);
        Map<String, Strategy> strategyMap = new HashMap<>();
        strategyMap.put("default", new DefaultStrategy());
        contextProvider = mock(UnleashContextProvider.class);
        eventDispatcher = mock(EventDispatcher.class);
        metricService = mock(UnleashMetricService.class);

        sut =
                new DefaultUnleash(
                        unleashConfig,
                        featureRepository,
                        strategyMap,
                        contextProvider,
                        eventDispatcher,
                        metricService);
    }

    @Test
    public void should_evaluate_all_toggle_with_context() {
        when(featureRepository.getFeatureNames()).thenReturn(asList("toggle1", "toggle2"));
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        List<EvaluatedToggle> toggles = sut.more().evaluateAllToggles();
        assertThat(toggles).hasSize(2);
        EvaluatedToggle t1 = toggles.get(0);
        assertThat(t1.getName()).isEqualTo("toggle1");
        assertThat(t1.isEnabled()).isFalse();
    }

    @Test
    public void should_count_and_not_throw_an_error() {
        sut.more().count("toggle1", true);
        sut.more().count("toggle1", false);

        verify(metricService).count("toggle1", true);
        verify(metricService).count("toggle1", false);
    }

    @Test
    public void should_countVariant_and_not_throw_an_error() {
        sut.more().countVariant("toggle1", "variant1");

        verify(metricService).countVariant("toggle1", "variant1");
    }

    @Test
    public void should_evaluate_missing_segment_as_false() {
        String toggleName = "F9.withMissingSegment";
        String semVer = "1.2.2";
        Constraint semverConstraint = new Constraint("version", Operator.SEMVER_EQ, semVer);
        ActivationStrategy withMissingSegment =
                new ActivationStrategy(
                        "default", Collections.emptyMap(), asList(semverConstraint), asList(404));
        when(featureRepository.getToggle(toggleName))
                .thenReturn(new FeatureToggle(toggleName, true, asList(withMissingSegment)));
        when(featureRepository.getSegment(404)).thenReturn(Segment.DENY_SEGMENT);
        when(contextProvider.getContext())
                .thenReturn(UnleashContext.builder().addProperty("version", semVer).build());
        assertThat(sut.isEnabled(toggleName)).isFalse();
    }

    @Test
    public void should_evaluate_segment_collection_with_one_missing_segment_as_false() {
        String toggleName = "F9.withMissingSegment";
        Constraint semverConstraint = new Constraint("version", Operator.SEMVER_EQ, "1.2.2");
        ActivationStrategy withMissingSegment =
                new ActivationStrategy(
                        "default",
                        Collections.emptyMap(),
                        asList(semverConstraint),
                        asList(404, 1));
        when(featureRepository.getToggle(toggleName))
                .thenReturn(new FeatureToggle(toggleName, true, asList(withMissingSegment)));
        when(featureRepository.getSegment(1))
                .thenReturn(
                        new Segment(
                                1,
                                "always true",
                                asList(
                                        new Constraint(
                                                "always_true",
                                                Operator.NOT_IN,
                                                Collections.EMPTY_LIST))));
        when(contextProvider.getContext())
                .thenReturn(UnleashContext.builder().addProperty("version", "1.2.2").build());
        assertThat(sut.isEnabled(toggleName)).isFalse();
    }

    @Test
    public void should_allow_fallback_strategy() {
        Strategy fallback = mock(Strategy.class);

        UnleashConfig unleashConfigWithFallback =
                UnleashConfig.builder()
                        .unleashAPI("http://fakeAPI")
                        .appName("fakeApp")
                        .fallbackStrategy(fallback)
                        .build();
        sut =
                new DefaultUnleash(
                        unleashConfigWithFallback,
                        featureRepository,
                        new HashMap<>(),
                        contextProvider,
                        eventDispatcher,
                        metricService);

        ActivationStrategy as = new ActivationStrategy("forFallback", new HashMap<>());
        FeatureToggle toggle = new FeatureToggle("toggle1", true, Collections.singletonList(as));
        when(featureRepository.getToggle("toggle1")).thenReturn(toggle);
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        sut.isEnabled("toggle1");

        verify(fallback).isEnabled(any(), any(), any());
    }

    @Test
    public void multiple_instantiations_of_the_same_config_gives_errors() {
        ListAppender<ILoggingEvent> appender = new ListAppender();
        appender.start();
        Logger unleashLogger = (Logger) LoggerFactory.getLogger(DefaultUnleash.class);
        unleashLogger.addAppender(appender);
        UnleashConfig config =
                UnleashConfig.builder()
                        .unleashAPI("http://test:4242")
                        .appName("multiple_connection")
                        .apiKey("default:development:1234567890123456")
                        .instanceId("testing-multiple")
                        .build();
        Unleash unleash1 = new DefaultUnleash(config);
        // We've only instantiated the client once, so no errors should've been logged
        assertThat(appender.list).isEmpty();
        Unleash unleash2 = new DefaultUnleash(config);
        // We've now instantiated the client twice, so we expect an error log line.
        assertThat(appender.list).hasSize(1);
        String id = config.getClientIdentifier();
        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(
                        "You already have 1 clients for Unleash Configuration ["
                                + id
                                + "] running. Please double check your code where you are instantiating the Unleash SDK");
        appender.list.clear();
        Unleash unleash3 = new DefaultUnleash(config);
        // We've now instantiated the client twice, so we expect an error log line.
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(
                        "You already have 2 clients for Unleash Configuration ["
                                + id
                                + "] running. Please double check your code where you are instantiating the Unleash SDK");
    }

    @Test
    public void supports_failing_hard_on_multiple_instantiations() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .unleashAPI("http://test:4242")
                        .appName("multiple_connection_with_failure")
                        .apiKey("default:development:1234567890123456")
                        .instanceId("testing-multiple")
                        .build();
        String id = config.getClientIdentifier();
        Unleash unleash1 = new DefaultUnleash(config);
        assertThatThrownBy(
                        () -> {
                            Unleash unleash2 =
                                    new DefaultUnleash(config, null, null, null, null, null, true);
                        })
                .isInstanceOf(RuntimeException.class)
                .withFailMessage(
                        "You already have 1 clients for Unleash Configuration ["
                                + id
                                + "] running. Please double check your code where you are instantiating the Unleash SDK");
    }
}
