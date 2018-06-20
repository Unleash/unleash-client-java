package no.finn.unleash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.UserWithIdStrategy;
import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnleashTest {

    private ToggleRepository toggleRepository;
    private UnleashContextProvider contextProvider;
    private Unleash unleash;

    @BeforeEach
    public void setup() {
        toggleRepository = mock(ToggleRepository.class);
        contextProvider = mock(UnleashContextProvider.class);
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        UnleashConfig config = new UnleashConfig.Builder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .unleashContextProvider(contextProvider)
                .build();

        unleash = new DefaultUnleash(config, toggleRepository, new UserWithIdStrategy());
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
        UnleashConfig config = new UnleashConfig.Builder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .build();
        unleash = new DefaultUnleash(config, toggleRepository, customStrategy);
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, Arrays.asList(new ActivationStrategy("custom", null))));

        unleash.isEnabled("test");

        verify(customStrategy, times(1)).isEnabled(anyMap(), any(UnleashContext.class));
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
    public void should_support_context_provider() {
        UnleashContext context = UnleashContext.builder().userId("111").build();
        when(contextProvider.getContext()).thenReturn(context);

        //Set up a toggle using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Arrays.asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(true));
    }
    
    @Test
    public void should_support_context_as_part_of_is_enabled_call() {
        UnleashContext context = UnleashContext.builder().userId("13").build();

        //Set up a toggle using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Arrays.asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test", context), is(true));
    }

    @Test
    public void inactive_feature_toggle() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, Arrays.asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void should_return_known_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, Arrays.asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(((DefaultUnleash)unleash).getFeatureToggleDefinition("test"), is(Optional.of(featureToggle)));
    }

    @Test
    public void should_return_empty_for_unknown_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, Arrays.asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(((DefaultUnleash)unleash).getFeatureToggleDefinition("another toggle"), is(Optional.empty()));
    }

    @Test
    public void get_feature_names_should_return_list_of_feature_names() {
        when(toggleRepository.getFeatureNames()).thenReturn(Arrays.asList("toggleFeatureName1", "toggleFeatureName2"));
        assertTrue(2 == unleash.getFeatureToggleNames().size());
        assertTrue("toggleFeatureName2".equals(unleash.getFeatureToggleNames().get(1)));
    }
}
