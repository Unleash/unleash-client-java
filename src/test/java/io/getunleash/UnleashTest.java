package io.getunleash;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.getunleash.engine.MetricsBucket;
import io.getunleash.event.EventDispatcher;
import io.getunleash.repository.*;
import io.getunleash.repository.UnleashEngineStateHandler;
import io.getunleash.strategy.Strategy;
import io.getunleash.strategy.UserWithIdStrategy;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import io.getunleash.variant.Payload;
import io.getunleash.variant.VariantDefinition;
import java.util.*;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class UnleashTest {

    private FeatureRepository toggleRepository;
    private UnleashContextProvider contextProvider;
    private Unleash unleash;

    private UnleashEngineStateHandler stateHandler;

    @BeforeEach
    public void setup() {
        toggleRepository = mock(FeatureRepository.class);

        contextProvider = mock(UnleashContextProvider.class);
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .environment("test")
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .unleashContextProvider(contextProvider)
                        .build();

        unleash = new DefaultUnleash(config, toggleRepository, new UserWithIdStrategy());
        stateHandler = new UnleashEngineStateHandler((DefaultUnleash) unleash);
    }

    @Test
    public void known_toogle_and_strategy_should_be_active() {
        stateHandler.setState(
                new FeatureToggle("test", true, asList(new ActivationStrategy("default", null))));
        assertThat(unleash.isEnabled("test")).isTrue();
    }

    @Test
    public void unknown_strategy_should_be_considered_inactive() {
        when(toggleRepository.getToggle("test"))
                .thenReturn(
                        new FeatureToggle(
                                "test", true, asList(new ActivationStrategy("whoot_strat", null))));

        assertThat(unleash.isEnabled("test")).isFalse();
    }

    @Test
    public void unknown_feature_should_be_considered_inactive() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test")).isFalse();
    }

    @Test
    public void unknown_feature_should_use_default_setting() {
        when(toggleRepository.getToggle("test")).thenReturn(null);

        assertThat(unleash.isEnabled("test", true)).isTrue();
    }

    @Test
    public void fallback_function_should_be_invoked_and_return_true() {
        when(toggleRepository.getToggle("test")).thenReturn(null);
        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(true);

        assertThat(unleash.isEnabled("test", fallbackAction)).isTrue();
        verify(fallbackAction, times(1)).test(anyString(), any(UnleashContext.class));
    }

    @Test
    public void fallback_function_should_be_invoked_also_with_context() {
        when(toggleRepository.getToggle("test")).thenReturn(null);
        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(true);

        UnleashContext context = UnleashContext.builder().userId("123").build();

        assertThat(unleash.isEnabled("test", context, fallbackAction)).isTrue();
        verify(fallbackAction, times(1)).test(anyString(), any(UnleashContext.class));
    }

    @Test
    void fallback_function_should_be_invoked_and_return_false() {
        when(toggleRepository.getToggle("test")).thenReturn(null);
        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(false);

        assertThat(unleash.isEnabled("test", fallbackAction)).isFalse();
        verify(fallbackAction, times(1)).test(anyString(), any(UnleashContext.class));
    }

    @Test
    void fallback_function_should_not_be_called_when_toggle_is_defined() {
        stateHandler.setState(
                new FeatureToggle("test", true, asList(new ActivationStrategy("default", null))));

        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(false);

        assertThat(unleash.isEnabled("test", fallbackAction)).isTrue();
        verify(fallbackAction, never()).test(anyString(), any(UnleashContext.class));
    }

    @Test
    public void should_register_custom_strategies() {
        // custom strategy
        Strategy customStrategy = mock(Strategy.class);
        when(customStrategy.getName()).thenReturn("custom");
        when(customStrategy.getResult(anyMap(), any(), anyList(), anyList())).thenCallRealMethod();

        // register custom strategy
        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .build();
        unleash = new DefaultUnleash(config, toggleRepository, customStrategy);
        new UnleashEngineStateHandler((DefaultUnleash) unleash)
                .setState(
                        new FeatureToggle(
                                "test", true, asList(new ActivationStrategy("custom", null))));
        unleash.isEnabled("test");

        // PR-comment: constraints are no longer managed by the SDK but by Yggdrasil, so
        // we removed
        // the third parameter
        verify(customStrategy, times(1)).isEnabled(any(), any(UnleashContext.class));
    }

    @Test
    public void should_support_multiple_strategies() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        ActivationStrategy activeStrategy = new ActivationStrategy("default", null);

        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy1, activeStrategy));

        stateHandler.setState(featureToggle);

        assertThat(unleash.isEnabled("test")).isTrue();
    }

    @Test
    public void shouldSupportMultipleRolloutStrategies() {
        Map<String, String> rollout100percent = new HashMap<>();
        rollout100percent.put("rollout", "100");
        rollout100percent.put("stickiness", "default");
        rollout100percent.put("groupId", "rollout");

        Constraint user6Constraint = new Constraint("userId", Operator.IN, singletonList("6"));
        Constraint user9Constraint = new Constraint("userId", Operator.IN, singletonList("9"));

        ActivationStrategy strategy1 =
                new ActivationStrategy(
                        "flexibleRollout",
                        rollout100percent,
                        singletonList(user6Constraint),
                        null,
                        null);
        ActivationStrategy strategy2 =
                new ActivationStrategy(
                        "flexibleRollout",
                        rollout100percent,
                        singletonList(user9Constraint),
                        null,
                        null);

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy1, strategy2));

        stateHandler.setState(featureToggle);

        assertThat(unleash.isEnabled("test", UnleashContext.builder().userId("1").build()))
                .isFalse();
        assertThat(unleash.isEnabled("test", UnleashContext.builder().userId("6").build()))
                .isTrue();
        assertThat(unleash.isEnabled("test", UnleashContext.builder().userId("7").build()))
                .isFalse();
        assertThat(unleash.isEnabled("test", UnleashContext.builder().userId("9").build()))
                .isTrue();
    }

    @Test
    public void should_support_context_provider() {
        UnleashContext context = UnleashContext.builder().userId("111").build();
        when(contextProvider.getContext()).thenReturn(context);

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        stateHandler.setState(featureToggle);

        assertThat(unleash.isEnabled("test")).isTrue();
    }

    @Test
    public void should_support_context_as_part_of_is_enabled_call() {
        UnleashContext context = UnleashContext.builder().userId("13").build();

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        stateHandler.setState(featureToggle);

        assertThat(unleash.isEnabled("test", context)).isTrue();
    }

    @Test
    public void should_support_context_as_part_of_is_enabled_call_and_use_default() {
        UnleashContext context = UnleashContext.builder().userId("13").build();

        // Set up a toggle using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");

        assertThat(unleash.isEnabled("test", context, true)).isTrue();
    }

    @Test
    public void inactive_feature_toggle() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test")).isFalse();
    }

    @Test
    public void should_return_known_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.more().getFeatureToggleDefinition("test")).hasValue(featureToggle);
    }

    @Test
    public void should_return_empty_for_unknown_feature_toggle_definition() {
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy1));
        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.more().getFeatureToggleDefinition("another toggleName")).isEmpty();
    }

    @Test
    public void get_feature_names_should_return_list_of_feature_names() {
        when(toggleRepository.getFeatureNames())
                .thenReturn(asList("toggleFeatureName1", "toggleFeatureName2"));
        assertThat(unleash.getFeatureToggleNames()).hasSize(2);
        assertThat(unleash.getFeatureToggleNames().get(1)).isEqualTo("toggleFeatureName2");
    }

    @Test
    public void get_default_variant_when_disabled() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result =
                unleash.getVariant("test", context, new Variant("Chuck", "Norris", true));

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Chuck");
        assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("Norris");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    public void getting_variant_when_disabled_should_increment_no_counter() {
        UnleashContext context = UnleashContext.builder().userId("1").build();
        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .environment("test")
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .unleashContextProvider(contextProvider)
                        .build();
        Unleash thisUnleash =
                new DefaultUnleash(
                        config,
                        toggleRepository,
                        Collections.emptyMap(),
                        contextProvider,
                        new EventDispatcher(config));
        UnleashEngineStateHandler localStateHandler =
                new UnleashEngineStateHandler((DefaultUnleash) thisUnleash);
        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", false, asList(strategy));

        localStateHandler.setState(featureToggle);

        final Variant result = thisUnleash.getVariant("test", context);

        assertThat(result).isNotNull();
        MetricsBucket bucket = localStateHandler.captureMetrics();
        assertThat(bucket.getToggles().get("test").getYes()).isEqualTo(0);
        assertThat(bucket.getToggles().get("test").getNo()).isEqualTo(1);
    }

    @Test
    public void get_default_empty_variant_when_disabled_and_no_default_value_is_specified() {
        UnleashContext context = UnleashContext.builder().userId("1").build();

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(strategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("disabled");
        assertThat(result.getPayload().map(Payload::getValue)).isEmpty();
        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    public void get_first_variant() {
        UnleashContext context = UnleashContext.builder().userId("356").build();

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 121, 356");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy), getTestVariants());

        stateHandler.setState(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("en");
        assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("en");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    public void get_second_variant() {
        UnleashContext context = UnleashContext.builder().userId("5").build();

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 5, 121, 13");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy), getTestVariants());

        stateHandler.setState(featureToggle);

        final Variant result = unleash.getVariant("test", context);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("to");
        assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("to");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    public void get_disabled_variant_without_context() {

        // Set up a toggleName using UserWithIdStrategy
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy1), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("disabled");
        assertThat(result.getPayload().map(Payload::getValue)).isEmpty();
        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    public void get_default_variant_without_context() {
        // Set up a toggleName using UserWithIdStrategy
        ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy1), getTestVariants());

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        final Variant result = unleash.getVariant("test", new Variant("Chuck", "Norris", true));

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Chuck");
        assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("Norris");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    public void get_first_variant_with_context_provider() {

        UnleashContext context = UnleashContext.builder().userId("356").build();
        when(contextProvider.getContext()).thenReturn(context);

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 111, 356");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy), getTestVariants());

        stateHandler.setState(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("en");
        assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("en");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    public void get_second_variant_with_context_provider() {

        UnleashContext context = UnleashContext.builder().userId("5").build();
        when(contextProvider.getContext()).thenReturn(context);

        // Set up a toggleName using UserWithIdStrategy
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123, 5, 121");
        ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
        FeatureToggle featureToggle =
                new FeatureToggle("test", true, asList(strategy), getTestVariants());

        stateHandler.setState(featureToggle);

        final Variant result = unleash.getVariant("test");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("to");
        assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("to");
        assertThat(result.isEnabled()).isTrue();
    }

    @Test
    public void should_be_enabled_with_strategy_constraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("test")));
        ActivationStrategy activeStrategy =
                new ActivationStrategy(
                        "default",
                        null,
                        constraints,
                        Collections.emptyList(),
                        Collections.emptyList());

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        stateHandler.setState(featureToggle);

        assertThat(unleash.isEnabled("test")).isTrue();
    }

    @Test
    public void should_be_disabled_with_strategy_constraints() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("dev", "prod")));
        ActivationStrategy activeStrategy =
                new ActivationStrategy(
                        "default",
                        null,
                        constraints,
                        Collections.emptyList(),
                        Collections.emptyList());

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test")).isFalse();
    }

    @Test
    public void should_handle_complex_segment_chains() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://unleash.org")
                        .backupFile(
                                getClass().getResource("/unleash-repo-v2-advanced.json").getFile())
                        .build();
        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);
        FeatureCollection featureCollection = backupHandler.read();

        stateHandler.setState(featureCollection);

        UnleashContext context =
                UnleashContext.builder()
                        .addProperty("wins", "6")
                        .addProperty("dateLastWin", "2022-06-01T12:00:00.000Z")
                        .addProperty("followers", "1500")
                        .addProperty("single", "true")
                        .addProperty("catOrDog", "cat")
                        .build();

        when(contextProvider.getContext()).thenReturn(context);
        assertThat(unleash.isEnabled("Test.variants")).isTrue();
    }

    @Test
    @Disabled // TODO: panicked at 'called `Result::unwrap()` on an `Err` value: Error {
    // variant:
    // ParsingError { positives: [and, or], negatives: [] }, location: Pos(64),
    // line_col:
    // Pos((1, 65)), path: None, line: "(true and (context[\"wins\"] > 5 and
    // context[\"dateLastWin\"] > 2022-05-01T12:00:00 and context[\"followers\"] >
    // 1000
    // and context[\"single\"] contains_any_ignore_case [\"true\"] and
    // context[\"catOrDog\"] contains_any_ignore_case [\"cat\"]))", continued_line:
    // None
    // }', unleash-yggdrasil/src/lib.rs:144:64
    public void should_handle_complex_segment_chains_2() {
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName("test")
                        .unleashAPI("http://http://unleash.org")
                        .backupFile(
                                getClass().getResource("/unleash-repo-v2-advanced.json").getFile())
                        .build();
        FeatureBackupHandlerFile backupHandler = new FeatureBackupHandlerFile(config);
        FeatureCollection featureCollection = backupHandler.read();

        Unleash overrideUnleash = new DefaultUnleash(config);
        new UnleashEngineStateHandler((DefaultUnleash) overrideUnleash).setState(featureCollection);

        UnleashContext context =
                UnleashContext.builder()
                        .addProperty("wins", "4")
                        .addProperty("dateLastWin", "2022-06-01T12:00:00.000Z")
                        .addProperty("followers", "900")
                        .addProperty("single", "false")
                        .addProperty("catOrDog", "dog")
                        .build();

        when(contextProvider.getContext()).thenReturn(context);
        assertThat(overrideUnleash.isEnabled("Test.variants")).isFalse();
    }

    @Test
    public void should_be_disabled_with_strategy_variants() {
        List<Constraint> constraints = new ArrayList<>();
        constraints.add(new Constraint("environment", Operator.IN, Arrays.asList("dev", "prod")));
        ActivationStrategy activeStrategy =
                new ActivationStrategy(
                        "default",
                        null,
                        constraints,
                        Collections.emptyList(),
                        Collections.emptyList());

        FeatureToggle featureToggle = new FeatureToggle("test", true, asList(activeStrategy));

        when(toggleRepository.getToggle("test")).thenReturn(featureToggle);

        assertThat(unleash.isEnabled("test")).isFalse();
    }

    private List<VariantDefinition> getTestVariants() {
        return asList(
                new VariantDefinition(
                        "en", 50, new Payload("string", "en"), Collections.emptyList()),
                new VariantDefinition(
                        "to", 50, new Payload("string", "to"), Collections.emptyList()));
    }

    private List<VariantDefinition> getTestVariantsForDeprecatedHash() {
        return asList(
                new VariantDefinition(
                        "en", 65, new Payload("string", "en"), Collections.emptyList()),
                new VariantDefinition(
                        "to", 35, new Payload("string", "to"), Collections.emptyList()));
    }
}
