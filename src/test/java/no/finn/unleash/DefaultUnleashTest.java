package no.finn.unleash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.finn.unleash.event.EventDispatcher;
import no.finn.unleash.metric.UnleashMetricService;
import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultUnleashTest {
    private DefaultUnleash sut;
    private ToggleRepository toggleRepository;
    private UnleashContextProvider contextProvider;
    private EventDispatcher eventDispatcher;
    private UnleashMetricService metricService;

    @BeforeEach
    public void setup() {
        UnleashConfig unleashConfig =
                UnleashConfig.builder().unleashAPI("http://fakeAPI").appName("fakeApp").build();
        toggleRepository = mock(ToggleRepository.class);
        Map<String, Strategy> strategyMap = new HashMap<>();
        contextProvider = mock(UnleashContextProvider.class);
        eventDispatcher = mock(EventDispatcher.class);
        metricService = mock(UnleashMetricService.class);

        sut =
                new DefaultUnleash(
                        unleashConfig,
                        toggleRepository,
                        strategyMap,
                        contextProvider,
                        eventDispatcher,
                        metricService);
    }

    @Test
    public void should_evaluate_all_toggle_with_context() {
        when(toggleRepository.getFeatureNames()).thenReturn(Arrays.asList("toggle1", "toggle2"));
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
}
