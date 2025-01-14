package io.getunleash;

import static io.getunleash.Variant.DISABLED_VARIANT;
import static java.util.Optional.ofNullable;

import io.getunleash.engine.*;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.IsEnabledImpressionEvent;
import io.getunleash.event.ToggleEvaluated;
import io.getunleash.event.VariantImpressionEvent;
import io.getunleash.lang.Nullable;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.metric.UnleashMetricServiceImpl;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.repository.IFeatureRepository;
import io.getunleash.repository.JsonFeatureParser;
import io.getunleash.strategy.*;
import io.getunleash.util.UnleashConfig;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUnleash implements Unleash {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUnleash.class);

    private static ConcurrentHashMap<String, LongAdder> initCounts = new ConcurrentHashMap<>();
    private static final List<Strategy> BUILTIN_STRATEGIES =
            Arrays.asList(
                    new DefaultStrategy(),
                    new ApplicationHostnameStrategy(),
                    new GradualRolloutRandomStrategy(),
                    new GradualRolloutSessionIdStrategy(),
                    new GradualRolloutUserIdStrategy(),
                    new RemoteAddressStrategy(),
                    new UserWithIdStrategy(),
                    new FlexibleRolloutStrategy());

    public static final UnknownStrategy UNKNOWN_STRATEGY = new UnknownStrategy();

    private final UnleashEngine unleashEngine;
    private final UnleashMetricService metricService;
    private final IFeatureRepository featureRepository;
    private final UnleashContextProvider contextProvider;
    private final EventDispatcher eventDispatcher;
    private final UnleashConfig config;

    private static IFeatureRepository defaultToggleRepository(UnleashConfig unleashConfig) {
        return new FeatureRepository(unleashConfig);
    }

    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        this(unleashConfig, defaultToggleRepository(unleashConfig), strategies);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig,
            IFeatureRepository featureRepository,
            Strategy... strategies) {
        this(
                unleashConfig,
                featureRepository,
                buildStrategyMap(strategies),
                unleashConfig.getContextProvider(),
                new EventDispatcher(unleashConfig),
                false);
    }

    // Visible for testing
    public DefaultUnleash(
            UnleashConfig unleashConfig,
            IFeatureRepository featureRepository,
            Map<String, Strategy> strategyMap,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher) {
        this(
                unleashConfig,
                featureRepository,
                strategyMap,
                contextProvider,
                eventDispatcher,
                false);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig,
            IFeatureRepository featureRepository,
            Map<String, Strategy> strategyMap,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher,
            boolean failOnMultipleInstantiations) {

        this.unleashEngine =
                new UnleashEngine(
                        strategyMap.values().stream()
                                .map(YggdrasilAdapters::adapt)
                                .collect(Collectors.toList()),
                        Optional.ofNullable(unleashConfig.getFallbackStrategy())
                                .map(YggdrasilAdapters::adapt)
                                .orElse(null));
        featureRepository.addConsumer(
                featureCollection -> {
                    try {
                        this.unleashEngine.takeState(
                                JsonFeatureParser.toJsonString(featureCollection));
                    } catch (YggdrasilInvalidInputException e) {
                        LOGGER.error("Unable to update features", e);
                    }
                });

        this.config = unleashConfig;
        this.featureRepository = featureRepository;
        this.contextProvider = contextProvider;
        this.eventDispatcher = eventDispatcher;
        this.metricService =
                new UnleashMetricServiceImpl(
                        unleashConfig, unleashConfig.getScheduledExecutor(), this.unleashEngine);
        metricService.register(strategyMap.keySet());
        initCounts.compute(
                config.getClientIdentifier(),
                (key, inits) -> {
                    if (inits != null) {
                        String error =
                                String.format(
                                        "You already have %d clients for AppName [%s] with instanceId: [%s] running. Please double check your code where you are instantiating the Unleash SDK",
                                        inits.sum(),
                                        unleashConfig.getAppName(),
                                        unleashConfig.getInstanceId());
                        if (failOnMultipleInstantiations) {
                            throw new RuntimeException(error);
                        } else {
                            LOGGER.error(error);
                        }
                    }
                    LongAdder a = inits == null ? new LongAdder() : inits;
                    a.increment();
                    return a;
                });
    }

    @Override
    public boolean isEnabled(final String toggleName, final boolean defaultSetting) {
        return isEnabled(toggleName, contextProvider.getContext(), defaultSetting);
    }

    @Override
    public boolean isEnabled(
            final String toggleName, final BiPredicate<String, UnleashContext> fallbackAction) {
        return isEnabled(toggleName, contextProvider.getContext(), fallbackAction);
    }

    @Override
    public boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction) {

        UnleashContext enhancedContext = context.applyStaticFields(config);
        try {
            Boolean enabled =
                    this.unleashEngine.isEnabled(
                            toggleName, YggdrasilAdapters.adapt(enhancedContext));
            if (enabled == null) {
                enabled = fallbackAction.test(toggleName, enhancedContext);
            }

            this.unleashEngine.countToggle(toggleName, enabled);
            eventDispatcher.dispatch(new ToggleEvaluated(toggleName, enabled));
            if (this.unleashEngine.shouldEmitImpressionEvent(toggleName)) {
                eventDispatcher.dispatch(
                        new IsEnabledImpressionEvent(toggleName, enabled, context));
            }
            return enabled;
        } catch (YggdrasilInvalidInputException | YggdrasilError e) {
            LOGGER.warn(
                    "A serious issue occurred when evaluating a feature toggle, defaulting to false",
                    e);
            return false;
        }
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, context, DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        UnleashContext enhancedContext = context.applyStaticFields(config);

        try {
            Context adaptedContext = YggdrasilAdapters.adapt(enhancedContext);

            Variant variant =
                    YggdrasilAdapters.adapt(
                            this.unleashEngine.getVariant(toggleName, adaptedContext),
                            defaultValue);

            this.unleashEngine.countToggle(toggleName, variant.isFeatureEnabled());
            this.unleashEngine.countVariant(toggleName, variant.getName());
            eventDispatcher.dispatch(new ToggleEvaluated(toggleName, variant.isEnabled()));
            if (unleashEngine.shouldEmitImpressionEvent(toggleName)) {
                eventDispatcher.dispatch(
                        new VariantImpressionEvent(
                                toggleName,
                                variant.isFeatureEnabled(),
                                context,
                                variant.getName()));
            }
            return variant;
        } catch (YggdrasilInvalidInputException | YggdrasilError e) {
            LOGGER.warn(
                    "A serious issue occurred when evaluating a variant, defaulting to the default value",
                    e);
            return defaultValue;
        }
    }

    @Override
    public Variant getVariant(String toggleName) {
        return getVariant(toggleName, contextProvider.getContext());
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        return getVariant(toggleName, contextProvider.getContext(), defaultValue);
    }

    /**
     * Use more().getFeatureToggleDefinition() instead
     *
     * @return the feature toggle
     */
    @Deprecated
    public Optional<FeatureToggle> getFeatureToggleDefinition(String toggleName) {
        return ofNullable(featureRepository.getToggle(toggleName));
    }

    /**
     * Use more().getFeatureToggleNames() instead
     *
     * @return a list of known toggle names
     */
    @Deprecated()
    public List<String> getFeatureToggleNames() {
        return featureRepository.getFeatureNames();
    }

    private static Map<String, Strategy> buildStrategyMap(@Nullable Strategy[] strategies) {
        Map<String, Strategy> map = new HashMap<>();

        BUILTIN_STRATEGIES.forEach(strategy -> map.put(strategy.getName(), strategy));

        if (strategies != null) {
            for (Strategy strategy : strategies) {
                map.put(strategy.getName(), strategy);
            }
        }

        return map;
    }

    @Override
    public void shutdown() {
        config.getScheduledExecutor().shutdown();
    }

    @Override
    public MoreOperations more() {
        return new DefaultMore();
    }

    public class DefaultMore implements MoreOperations {

        @Override
        public List<String> getFeatureToggleNames() {
            return featureRepository.getFeatureNames();
        }

        @Override
        public Optional<FeatureToggle> getFeatureToggleDefinition(String toggleName) {
            return ofNullable(featureRepository.getToggle(toggleName));
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles() {
            return evaluateAllToggles(contextProvider.getContext());
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles(UnleashContext context) {
            return getFeatureToggleNames().stream()
                    .map(
                            toggleName -> {
                                boolean enabled =
                                        isEnabled(toggleName, context, (name, ctx) -> false);
                                Variant variant = getVariant(toggleName, context, DISABLED_VARIANT);
                                return new EvaluatedToggle(toggleName, enabled, variant);
                            })
                    .collect(Collectors.toList());
        }
    }
}
