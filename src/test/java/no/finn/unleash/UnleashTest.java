package no.finn.unleash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.UserWithIdStrategy;
import no.finn.unleash.util.UnleashConfig;

import no.finn.unleash.variant.Payload;
import no.finn.unleash.variant.VariantDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
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
                .environment("test")
                .unleashContextProvider(contextProvider)
                .build();

        unleash = new DefaultUnleash(config, toggleRepository, new UserWithIdStrategy());
    }

    @Test
    public void known_toogle_and_strategy_should_be_active() {
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(new ActivationStrategy("default", null))));

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void unknown_strategy_should_be_considered_inactive() {
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(new ActivationStrategy("whoot_strat", null))));

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
    public void fallback_function_should_be_invoked() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test", (name, unleashContext) -> true), is(true));
    }

    @Test
    void fallback_function_should_override_default_fallback_value_when_toggle_not_defined() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test", (name, unleashContext) -> true), is(true));
    }

	@Test
	void fallback_function_should_not_be_called_when_toggle_is_defined() {
		when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(new ActivationStrategy("default", null))));

		assertThat(unleash.isEnabled("test", (name, unleashContext) -> false), is(true));
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
        when(toggleRepository.getToggle("test")).thenReturn(new FeatureToggle("test", true, asList(new ActivationStrategy("custom", null))));

        unleash.isEnabled("test");

        verify(customStrategy, times(1)).isEnabled(isNull(), any(UnleashContext.class), any(List.class));
    }

    @Test
    public void should_support_multiple_strategies() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null);

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1, activeStrategy));

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
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

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
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

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
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(false));
    }

    @Test
    public void should_return_known_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(((DefaultUnleash)unleash).getFeatureToggleDefinition("test"), is(Optional.of(featureToggle)));
    }

    @Test
    public void should_return_empty_for_unknown_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(((DefaultUnleash)unleash).getFeatureToggleDefinition("another toggleName"), is(Optional.empty()));
    }

    @Test
    public void get_feature_names_should_return_list_of_feature_names() {
        when(toggleRepository.getFeatureNames()).thenReturn(asList("toggleFeatureName1", "toggleFeatureName2"));
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
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context, new Variant("Chuck", "Norris", true));

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("Chuck"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("Norris"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_default_empty_variant_when_disabled_and_no_default_value_is_specified() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("disabled"));
        assertThat(result.getPayload().map(Payload::getValue), is(Optional.empty()));
        assertThat(result.isEnabled(), is(false));
    }

    @Test
    public void get_first_variant() {
        UnleashContext context = UnleashContext.builder().userId("356").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 356");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("en"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("en"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_second_variant() {
        UnleashContext context = UnleashContext.builder().userId("111").build();

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("to"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("to"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_disabled_variant_without_context() {

        //Set up a toggleName using UserWithIdStrategy
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("disabled"));
        assertThat(result.getPayload().map(Payload::getValue), is(Optional.empty()));
        assertThat(result.isEnabled(), is(false));
    }

    @Test
    public void get_default_variant_without_context() {
        //Set up a toggleName using UserWithIdStrategy
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", new Variant("Chuck", "Norris", true));

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("Chuck"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("Norris"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_first_variant_with_context_provider() {

        UnleashContext context = UnleashContext.builder().userId("356").build();
        when(contextProvider.getContext()).thenReturn(context);

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 356");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("en"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("en"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void get_second_variant_with_context_provider() {

        UnleashContext context = UnleashContext.builder().userId("111").build();
        when(contextProvider.getContext()).thenReturn(context);

        //Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result, is(notNullValue()));
        assertThat(result.getName(), is("to"));
        assertThat(result.getPayload().map(Payload::getValue).get(), is("to"));
        assertThat(result.isEnabled(), is(true));
    }

    @Test
    public void should_be_enabled_with_strategy_constraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test")));
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null, constraints);

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(true));
    }

    @Test
    public void should_be_disabled_with_strategy_constraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("dev", "prod")));
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null, constraints);

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test"), is(false));
    }


    private List<VariantDefinition> getTestVariants() {
        return asList(
            new VariantDefinition("en", 50, new Payload("string", "en"), Collections.emptyList()),
            new VariantDefinition("to", 50, new Payload("string", "to"), Collections.emptyList())
        );
    }
}
