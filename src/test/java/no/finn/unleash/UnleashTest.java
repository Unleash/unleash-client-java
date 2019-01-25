package no.finn.unleash;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.UserWithIdStrategy;
import no.finn.unleash.strategy.Variant;
import no.finn.unleash.util.UnleashConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
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

        //Set up a toggleName using UserWithIdStrategy
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

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Arrays.asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test", context), is(true));
    }

    @Test
    public void should_support_context_as_part_of_is_enabled_call_and_use_default() {
        UnleashContext context = UnleashContext.builder().userId("13").build();

        //Set up a toggle using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");

        assertThat(unleash.isEnabled("test", context, true), is(true));
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

        assertThat(((DefaultUnleash)unleash).getFeatureToggleDefinition("another toggleName"), is(Optional.empty()));
    }

    @Test
    public void get_feature_names_should_return_list_of_feature_names() {
        when(toggleRepository.getFeatureNames()).thenReturn(Arrays.asList("toggleFeatureName1", "toggleFeatureName2"));
        assertTrue(2 == unleash.getFeatureToggleNames().size());
        assertTrue("toggleFeatureName2".equals(unleash.getFeatureToggleNames().get(1)));
    }

    @Test
    public void get_default_variant_when_disabled() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Arrays.asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context, new Variant("Chuck", "Norris", true));

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("Chuck"));
        assertThat(result.getPayload(), is("Norris"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_default_empty_variant_when_disabled_and_no_default_value_is_specified() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Collections.singletonList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("disabled"));
        assertThat(result.getPayload(), is(nullValue()));
        assertThat(result.isEnabled(), is(false));
    }

    @Test
    public void get_first_variant() {
        UnleashContext context = UnleashContext.builder().userId("111").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Collections.singletonList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("en"));
        assertThat(result.getPayload(), is("en"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_second_variant() {
        UnleashContext context = UnleashContext.builder().userId("123").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, Collections.singletonList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("to"));
        assertThat(result.getPayload(), is("to"));
        assertThat(result.isEnabled(), is(true));
    }

    private List<VariantDefinition> getTestVariants() {
        return Arrays.asList(
            new VariantDefinition("en", 50, "en"),
            new VariantDefinition("to", 50, "to")
        );
    }
}
