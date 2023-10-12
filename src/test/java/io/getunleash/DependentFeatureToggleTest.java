package io.getunleash;

import io.getunleash.event.EventDispatcher;
import io.getunleash.event.IsEnabledImpressionEvent;
import io.getunleash.event.VariantImpressionEvent;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.strategy.DefaultStrategy;
import io.getunleash.strategy.Strategy;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.VariantDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class DependentFeatureToggleTest {
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
    public void should_not_increment_count_for_parent_toggle_when_checking_child_toggle() {
        FeatureToggle child = new FeatureToggle("child", true, singletonList(new ActivationStrategy("default", null)), Collections.emptyList(), false,
            singletonList(new FeatureDependency("parent")));
        FeatureToggle parent = new FeatureToggle("parent", true, singletonList(new ActivationStrategy("default", null)), Collections.emptyList(), false);
        when(featureRepository.getToggle("child")).thenReturn(child);
        when(featureRepository.getToggle("parent")).thenReturn(parent);
        boolean enabled = sut.isEnabled("child", UnleashContext.builder().userId("7").build());
        assertThat(enabled).isTrue();
        verify(metricService).count("child", true);
        verify(metricService, never()).count(eq("parent"), anyBoolean());
    }
    @Test
    public void should_not_increment_count_for_parent_toggle_when_checking_parent_variants() {
        FeatureToggle child = new FeatureToggle("child", true, singletonList(new ActivationStrategy("default", null)), singletonList(new VariantDefinition("childVariant", 1, null, null)), false,
            singletonList(new FeatureDependency("parent", true, singletonList("first"))));
        FeatureToggle parent = new FeatureToggle("parent", true, singletonList(new ActivationStrategy("default", null)), singletonList(new VariantDefinition("first", 1, null, null, null)), false);
        when(featureRepository.getToggle("child")).thenReturn(child);
        when(featureRepository.getToggle("parent")).thenReturn(parent);
        Variant variant = sut.getVariant("child", UnleashContext.builder().userId("7").build());
        assertThat(variant).isNotNull();
        verify(metricService).countVariant("child", "childVariant");
        verify(metricService, never()).countVariant("parent", "first");
    }

    @Test
    public void should_trigger_impression_event_for_parent_toggle_when_checking_child_toggle() {
        FeatureToggle child = new FeatureToggle("child", true, singletonList(new ActivationStrategy("default", null)), Collections.emptyList(), false,
            singletonList(new FeatureDependency("parent")));
        FeatureToggle parent = new FeatureToggle("parent", true, singletonList(new ActivationStrategy("default", null)), Collections.emptyList(), true);
        when(featureRepository.getToggle("child")).thenReturn(child);
        when(featureRepository.getToggle("parent")).thenReturn(parent);
        boolean enabled = sut.isEnabled("child", UnleashContext.builder().userId("7").build());
        assertThat(enabled).isTrue();
        verify(eventDispatcher).dispatch(any(IsEnabledImpressionEvent.class));
    }

    @Test
    public void should_trigger_impression_event_for_parent_variant_when_checking_child_toggle() {
        FeatureToggle child = new FeatureToggle("child", true, singletonList(new ActivationStrategy("default", null)), singletonList(new VariantDefinition("childVariant", 1, null, null)), true,
            singletonList(new FeatureDependency("parent", true, singletonList("first"))));
        FeatureToggle parent = new FeatureToggle("parent", true, singletonList(new ActivationStrategy("default", null)), singletonList(new VariantDefinition("first", 1, null, null, null)), true);
        when(featureRepository.getToggle("child")).thenReturn(child);
        when(featureRepository.getToggle("parent")).thenReturn(parent);
        Variant variant = sut.getVariant("child", UnleashContext.builder().userId("7").build());
        assertThat(variant).isNotNull();
        verify(eventDispatcher, times(2)).dispatch(any(VariantImpressionEvent.class));
    }
}
