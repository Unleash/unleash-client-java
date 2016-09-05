package no.finn.unleash;

import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.Strategy;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class UnleashTest {

    private ToggleRepository toggleRepository;
    private Unleash unleash;

    @Before
    public void setup() {
        toggleRepository = mock(ToggleRepository.class);
        unleash = new DefaultUnleash(toggleRepository);
    }

    @Test
    public void known_toogle_and_strategy_should_be_active() {
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, Arrays.asList(new ActivationStrategy("default", null))));

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void unknown_strategy_should_be_considered_inactive() {
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, Arrays.asList(new ActivationStrategy("whoot_strat", null))));

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void unknown_feature_should_be_considered_inactive() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void unknown_feature_should_use_default_setting() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test", true), is(true));
    }

    @Test
    public void should_register_custom_strategies() {
        //custom strategy
        Strategy customStrategy = mock(Strategy.class);
        when(customStrategy.getName()).thenReturn("custom");

        //register custom strategy
        unleash = new DefaultUnleash(toggleRepository, customStrategy);
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, Arrays.asList(new ActivationStrategy("custom", null))));

        unleash.isEnabled("test");

        verify(customStrategy, times(1)).isEnabled(anyMap());
    }

    @Test
    public void should_support_multiple_strategies() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null);

        FeatureToggle featureToggle = new FeatureToggle("test", true, Arrays.asList(strategy1, activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void inactive_feature_toggle() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, Arrays.asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(false));
    }
}
