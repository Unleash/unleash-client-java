package io.getunleash;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.getunleash.repository.*;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.*;
import java.util.function.BiPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UnleashTest {

    private EngineProxyImpl engineProxy;
    private UnleashContextProvider contextProvider;
    private UnleashConfig baseConfig;

    @BeforeEach
    public void setup() {

        contextProvider = mock(UnleashContextProvider.class);
        when(contextProvider.getContext()).thenReturn(UnleashContext.builder().build());

        baseConfig =
                new UnleashConfig.Builder()
                        .appName("test")
                        .environment("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .disableMetrics()
                        .disablePolling()
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .unleashContextProvider(contextProvider)
                        .build();
    }

    @Test
    public void unknown_feature_should_use_default_setting() {
        EngineProxy engineProxy = mock(EngineProxy.class);
        when(engineProxy.isEnabled(eq("test"), any(UnleashContext.class))).thenReturn(null);
        Unleash unleash = new DefaultUnleash(baseConfig, engineProxy);

        boolean featureEnabled = unleash.isEnabled("test", true);

        assertThat(featureEnabled).isTrue();
    }

    @Test
    public void fallback_function_should_be_invoked_and_return_true() {
        EngineProxy engineProxy = mock(EngineProxy.class);
        when(engineProxy.isEnabled(eq("test"), any(UnleashContext.class))).thenReturn(null);
        Unleash unleash = new DefaultUnleash(baseConfig, engineProxy);

        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(true);

        assertThat(unleash.isEnabled("test", fallbackAction)).isTrue();
        verify(fallbackAction, times(1)).test(anyString(), any(UnleashContext.class));
    }

    @Test
    public void fallback_function_should_be_invoked_also_with_context() {
        EngineProxy engineProxy = mock(EngineProxy.class);
        when(engineProxy.isEnabled(eq("test"), any(UnleashContext.class))).thenReturn(null);
        Unleash unleash = new DefaultUnleash(baseConfig, engineProxy);

        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(true);

        UnleashContext context = UnleashContext.builder().userId("123").build();

        assertThat(unleash.isEnabled("test", context, fallbackAction)).isTrue();
        verify(fallbackAction, times(1)).test(anyString(), any(UnleashContext.class));
    }

    @Test
    void fallback_function_should_be_invoked_and_return_false() {
        EngineProxy engineProxy = mock(EngineProxy.class);
        when(engineProxy.isEnabled(eq("test"), any(UnleashContext.class))).thenReturn(null);
        Unleash unleash = new DefaultUnleash(baseConfig, engineProxy);

        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(false);

        assertThat(unleash.isEnabled("test", fallbackAction)).isFalse();
        verify(fallbackAction, times(1)).test(anyString(), any(UnleashContext.class));
    }

    @Test
    void fallback_function_should_not_be_called_when_toggle_is_defined() {
        EngineProxy engineProxy = mock(EngineProxy.class);
        when(engineProxy.isEnabled(eq("test"), any(UnleashContext.class))).thenReturn(true);
        Unleash unleash = new DefaultUnleash(baseConfig, engineProxy);

        BiPredicate<String, UnleashContext> fallbackAction = mock(BiPredicate.class);
        when(fallbackAction.test(eq("test"), any(UnleashContext.class))).thenReturn(false);

        assertThat(unleash.isEnabled("test", fallbackAction)).isTrue();
        verify(fallbackAction, never()).test(anyString(), any(UnleashContext.class));
    }

    // @Test
    // public void should_register_custom_strategies() {
    //         // custom strategy
    //         Strategy customStrategy = mock(Strategy.class);
    //         when(customStrategy.getName()).thenReturn("custom");
    //         when(customStrategy.getResult(anyMap(), any(), anyList(),
    //                         anyList())).thenCallRealMethod();

    //         // setup a bootstrapper so our custom strategy is hydrated into the engine
    //         ToggleBootstrapProvider bootstrapper = new ToggleBootstrapProvider() {
    //                 @Override
    //                 public String read() {
    //                         return "{\"strategies\":[{\"name\":\"custom\"}]}";
    //                 }
    //         };

    //         UnleashConfig config = new UnleashConfig.Builder()
    //                         .appName("test")
    //                         .unleashAPI("http://localhost:4242/api/")
    //                         .build();

    //         Unleash unleash = new DefaultUnleash(config, customStrategy);
    //         // new UnleashEngineStateHandler((DefaultUnleash) unleash)
    //         //                 .setState(
    //         //                                 new FeatureToggle(
    //         //                                                 "test", true,
    //         //                                                 asList(new
    // ActivationStrategy("custom", null))));
    //         unleash.isEnabled("test");

    //         verify(customStrategy, times(1)).isEnabled(any(),
    //                         any(UnleashContext.class));
    // }

    // @Test
    // public void should_support_multiple_strategies() {
    // ActivationStrategy strategy1 = new ActivationStrategy("unknown", null);
    // ActivationStrategy activeStrategy = new ActivationStrategy("default", null);

    // FeatureToggle featureToggle =
    // new FeatureToggle("test", true, asList(strategy1, activeStrategy));

    // stateHandler.setState(featureToggle);

    // assertThat(unleash.isEnabled("test")).isTrue();
    // }

    // @Test
    // public void shouldSupportMultipleRolloutStrategies() {
    // Map<String, String> rollout100percent = new HashMap<>();
    // rollout100percent.put("rollout", "100");
    // rollout100percent.put("stickiness", "default");
    // rollout100percent.put("groupId", "rollout");

    // Constraint user6Constraint = new Constraint("userId", Operator.IN,
    // singletonList("6"));
    // Constraint user9Constraint = new Constraint("userId", Operator.IN,
    // singletonList("9"));

    // ActivationStrategy strategy1 =
    // new ActivationStrategy(
    // "flexibleRollout",
    // rollout100percent,
    // singletonList(user6Constraint),
    // null,
    // null);
    // ActivationStrategy strategy2 =
    // new ActivationStrategy(
    // "flexibleRollout",
    // rollout100percent,
    // singletonList(user9Constraint),
    // null,
    // null);

    // FeatureToggle featureToggle = new FeatureToggle("test", true,
    // asList(strategy1, strategy2));

    // stateHandler.setState(featureToggle);

    // assertThat(unleash.isEnabled("test",
    // UnleashContext.builder().userId("1").build()))
    // .isFalse();
    // assertThat(unleash.isEnabled("test",
    // UnleashContext.builder().userId("6").build()))
    // .isTrue();
    // assertThat(unleash.isEnabled("test",
    // UnleashContext.builder().userId("7").build()))
    // .isFalse();
    // assertThat(unleash.isEnabled("test",
    // UnleashContext.builder().userId("9").build()))
    // .isTrue();
    // }

    // @Test
    // public void should_support_context_provider() {
    // UnleashContext context = UnleashContext.builder().userId("111").build();
    // when(contextProvider.getContext()).thenReturn(context);

    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 111, 121");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle = new FeatureToggle("test", true,
    // asList(strategy));

    // stateHandler.setState(featureToggle);

    // assertThat(unleash.isEnabled("test")).isTrue();
    // }

    // @Test
    // public void should_support_context_as_part_of_is_enabled_call() {
    // UnleashContext context = UnleashContext.builder().userId("13").build();

    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 111, 121, 13");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle = new FeatureToggle("test", true,
    // asList(strategy));

    // stateHandler.setState(featureToggle);

    // assertThat(unleash.isEnabled("test", context)).isTrue();
    // }

    // @Test
    // public void
    // should_support_context_as_part_of_is_enabled_call_and_use_default() {
    // UnleashContext context = UnleashContext.builder().userId("13").build();

    // // Set up a toggle using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 111, 121, 13");

    // assertThat(unleash.isEnabled("test", context, true)).isTrue();
    // }

    // @Test
    // public void get_default_variant_when_disabled() {
    // UnleashContext context = UnleashContext.builder().userId("1").build();

    // stateHandler.setState(
    // new FeatureToggle(
    // "toggleFeatureName1",
    // true,
    // asList(new ActivationStrategy("default", null))));

    // final Variant result =
    // unleash.getVariant("test", context, new Variant("Chuck", "Norris", true));

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("Chuck");
    // assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("Norris");
    // assertThat(result.isEnabled()).isTrue();
    // }

    // @Test
    // public void getting_variant_when_disabled_should_increment_no_counter() {
    // UnleashContext context = UnleashContext.builder().userId("1").build();
    // UnleashConfig config =
    // new UnleashConfig.Builder()
    // .appName("test")
    // .unleashAPI("http://localhost:4242/api/")
    // .environment("test")
    // .scheduledExecutor(mock(UnleashScheduledExecutor.class))
    // .unleashContextProvider(contextProvider)
    // .build();
    // Unleash thisUnleash =
    // new DefaultUnleash(
    // config, engineProxy, contextProvider, new EventDispatcher(config));
    // UnleashEngineStateHandler localStateHandler =
    // new UnleashEngineStateHandler((DefaultUnleash) thisUnleash);
    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 111, 121, 13");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle = new FeatureToggle("test", false,
    // asList(strategy));

    // localStateHandler.setState(featureToggle);

    // final Variant result = thisUnleash.getVariant("test", context);

    // assertThat(result).isNotNull();
    // MetricsBucket bucket = localStateHandler.captureMetrics();
    // assertThat(bucket.getToggles().get("test").getYes()).isEqualTo(0);
    // assertThat(bucket.getToggles().get("test").getNo()).isEqualTo(1);
    // }

    // @Test
    // public void
    // get_default_empty_variant_when_disabled_and_no_default_value_is_specified() {
    // UnleashContext context = UnleashContext.builder().userId("1").build();

    // stateHandler.setState(
    // new FeatureToggle("test", false, asList(new ActivationStrategy("default",
    // null))));

    // final Variant result = unleash.getVariant("test", context);

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("disabled");
    // assertThat(result.getPayload().map(Payload::getValue)).isEmpty();
    // assertThat(result.isEnabled()).isFalse();
    // }

    // @Test
    // public void get_first_variant() {
    // UnleashContext context = UnleashContext.builder().userId("356").build();

    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 111, 121, 356");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle =
    // new FeatureToggle("test", true, asList(strategy), getTestVariants());

    // stateHandler.setState(featureToggle);

    // final Variant result = unleash.getVariant("test", context);

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("en");
    // assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("en");
    // assertThat(result.isEnabled()).isTrue();
    // }

    // @Test
    // public void get_second_variant() {
    // UnleashContext context = UnleashContext.builder().userId("5").build();

    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 5, 121, 13");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle =
    // new FeatureToggle("test", true, asList(strategy), getTestVariants());

    // stateHandler.setState(featureToggle);

    // final Variant result = unleash.getVariant("test", context);

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("to");
    // assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("to");
    // assertThat(result.isEnabled()).isTrue();
    // }

    // @Test
    // public void get_disabled_variant_without_context() {

    // stateHandler.setState(
    // new FeatureToggle("test", true, asList(new ActivationStrategy("default",
    // null))));

    // final Variant result = unleash.getVariant("test");

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("disabled");
    // assertThat(result.getPayload().map(Payload::getValue)).isEmpty();
    // assertThat(result.isEnabled()).isFalse();
    // }

    // @Test
    // public void get_first_variant_with_context_provider() {

    // UnleashContext context = UnleashContext.builder().userId("356").build();
    // when(contextProvider.getContext()).thenReturn(context);

    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 111, 356");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle =
    // new FeatureToggle("test", true, asList(strategy), getTestVariants());

    // stateHandler.setState(featureToggle);

    // final Variant result = unleash.getVariant("test");

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("en");
    // assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("en");
    // assertThat(result.isEnabled()).isTrue();
    // }

    // @Test
    // public void get_second_variant_with_context_provider() {

    // UnleashContext context = UnleashContext.builder().userId("5").build();
    // when(contextProvider.getContext()).thenReturn(context);

    // // Set up a toggleName using UserWithIdStrategy
    // Map<String, String> params = new HashMap<>();
    // params.put("userIds", "123, 5, 121");
    // ActivationStrategy strategy = new ActivationStrategy("userWithId", params);
    // FeatureToggle featureToggle =
    // new FeatureToggle("test", true, asList(strategy), getTestVariants());

    // stateHandler.setState(featureToggle);

    // final Variant result = unleash.getVariant("test");

    // assertThat(result).isNotNull();
    // assertThat(result.getName()).isEqualTo("to");
    // assertThat(result.getPayload().map(Payload::getValue).get()).isEqualTo("to");
    // assertThat(result.isEnabled()).isTrue();
    // }

    // @Test
    // public void should_be_enabled_with_strategy_constraints() {
    // List<Constraint> constraints = new ArrayList<>();
    // constraints.add(new Constraint("environment", Operator.IN,
    // Arrays.asList("test")));
    // ActivationStrategy activeStrategy =
    // new ActivationStrategy(
    // "default",
    // null,
    // constraints,
    // Collections.emptyList(),
    // Collections.emptyList());

    // FeatureToggle featureToggle = new FeatureToggle("test", true,
    // asList(activeStrategy));

    // stateHandler.setState(featureToggle);

    // assertThat(unleash.isEnabled("test")).isTrue();
    // }

    // @Test
    // public void should_handle_complex_segment_chains() {
    // UnleashConfig config =
    // UnleashConfig.builder()
    // .appName("test")
    // .unleashAPI("http://unleash.org")
    // .backupFile(
    // getClass().getResource("/unleash-repo-v2-advanced.json").getFile())
    // .build();
    // FeatureBackupHandlerFile backupHandler = new
    // FeatureBackupHandlerFile(config);
    // FeatureCollection featureCollection = backupHandler.read();

    // stateHandler.setState(featureCollection);

    // UnleashContext context =
    // UnleashContext.builder()
    // .addProperty("wins", "6")
    // .addProperty("dateLastWin", "2022-06-01T12:00:00.000Z")
    // .addProperty("followers", "1500")
    // .addProperty("single", "true")
    // .addProperty("catOrDog", "cat")
    // .build();

    // when(contextProvider.getContext()).thenReturn(context);
    // assertThat(unleash.isEnabled("Test.variants")).isTrue();
    // }

    // @Test
    // public void should_handle_complex_segment_chains_2() {
    // UnleashConfig config =
    // UnleashConfig.builder()
    // .appName("test")
    // .unleashAPI("http://http://unleash.org")
    // .backupFile(
    // getClass().getResource("/unleash-repo-v2-advanced.json").getFile())
    // .build();
    // FeatureBackupHandlerFile backupHandler = new
    // FeatureBackupHandlerFile(config);
    // FeatureCollection featureCollection = backupHandler.read();

    // Unleash overrideUnleash = new DefaultUnleash(config);
    // new UnleashEngineStateHandler((DefaultUnleash)
    // overrideUnleash).setState(featureCollection);

    // UnleashContext context =
    // UnleashContext.builder()
    // .addProperty("wins", "4")
    // .addProperty("dateLastWin", "2022-06-01T12:00:00.000Z")
    // .addProperty("followers", "900")
    // .addProperty("single", "false")
    // .addProperty("catOrDog", "dog")
    // .build();

    // when(contextProvider.getContext()).thenReturn(context);
    // assertThat(overrideUnleash.isEnabled("Test.variants")).isFalse();
    // }

    // @Test
    // public void empty_variants_returns_disabled_variant() {
    // UnleashContext context = UnleashContext.builder().build();

    // Map<String, String> params = new HashMap<>();
    // params.put("rollout", "100");
    // params.put("stickiness", "default");
    // params.put("groupId", "test");

    // ActivationStrategy strategy = new ActivationStrategy("flexibleRollout",
    // params);
    // FeatureToggle featureToggle =
    // new FeatureToggle("test", true, asList(strategy), Collections.emptyList());

    // stateHandler.setState(featureToggle);
    // final Variant result = unleash.getVariant("test", context);

    // assertThat(result.getName()).isEqualTo("disabled");
    // assertThat(result.isEnabled()).isFalse();
    // assertThat(result.isFeatureEnabled()).isTrue();
    // }

    // private List<VariantDefinition> getTestVariants() {
    // return asList(
    // new VariantDefinition(
    // "en", 50, new Payload("string", "en"), Collections.emptyList()),
    // new VariantDefinition(
    // "to", 50, new Payload("string", "to"), Collections.emptyList()));
    // }
}
