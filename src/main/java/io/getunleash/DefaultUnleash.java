package io.getunleash;

import static io.getunleash.Variant.DISABLED_VARIANT;
import static java.util.Optional.ofNullable;

import io.getunleash.event.*;
import io.getunleash.lang.Nullable;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.metric.UnleashMetricServiceImpl;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.repository.IFeatureRepository;
import io.getunleash.strategy.*;
import io.getunleash.util.ConstraintMerger;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.VariantUtil;
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

    private final UnleashMetricService metricService;
    private final IFeatureRepository featureRepository;
    private final Map<String, Strategy> strategyMap;
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
                new UnleashMetricServiceImpl(unleashConfig, unleashConfig.getScheduledExecutor()),
                false);
    }

    // Visible for testing
    public DefaultUnleash(
            UnleashConfig unleashConfig,
            IFeatureRepository featureRepository,
            Map<String, Strategy> strategyMap,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher,
            UnleashMetricService metricService) {
        this(
                unleashConfig,
                featureRepository,
                strategyMap,
                contextProvider,
                eventDispatcher,
                metricService,
                false);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig,
            IFeatureRepository featureRepository,
            Map<String, Strategy> strategyMap,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher,
            UnleashMetricService metricService,
            boolean failOnMultipleInstantiations) {
        this.config = unleashConfig;
        this.featureRepository = featureRepository;
        this.strategyMap = strategyMap;
        this.contextProvider = contextProvider;
        this.eventDispatcher = eventDispatcher;
        this.metricService = metricService;
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
        FeatureEvaluationResult result =
                getFeatureEvaluationResult(toggleName, context, fallbackAction, null);
        count(toggleName, result.isEnabled());
        eventDispatcher.dispatch(new ToggleEvaluated(toggleName, result.isEnabled()));
        dispatchEnabledImpressionDataIfNeeded("isEnabled", toggleName, result.isEnabled(), context);
        return result.isEnabled();
    }

    private void dispatchEnabledImpressionDataIfNeeded(
            String eventType, String toggleName, boolean enabled, UnleashContext context) {
        FeatureToggle toggle = featureRepository.getToggle(toggleName);
        if (toggle != null && toggle.hasImpressionData()) {
            eventDispatcher.dispatch(new IsEnabledImpressionEvent(toggleName, enabled, context));
        }
    }

    private FeatureEvaluationResult getFeatureEvaluationResult(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction,
            @Nullable Variant defaultVariant) {
        checkIfToggleMatchesNamePrefix(toggleName);
        FeatureToggle featureToggle = featureRepository.getToggle(toggleName);

        UnleashContext enhancedContext = context.applyStaticFields(config);
        Optional<ActivationStrategy> enabledStrategy = Optional.empty();
        boolean enabled = false;
        if (featureToggle == null) {
            enabled = fallbackAction.test(toggleName, enhancedContext);
        } else if (!featureToggle.isEnabled()) {
            enabled = false;
        } else if (featureToggle.getStrategies().size() == 0) {
            enabled = true;
        } else {
            enabledStrategy =
                    featureToggle.getStrategies().stream()
                            .filter(
                                    strategy -> {
                                        Strategy configuredStrategy =
                                                getStrategy(strategy.getName());
                                        if (configuredStrategy == UNKNOWN_STRATEGY) {
                                            LOGGER.warn(
                                                    "Unable to find matching strategy for toggle:{} strategy:{}",
                                                    toggleName,
                                                    strategy.getName());
                                        }

                                        return configuredStrategy.isEnabled(
                                                strategy.getParameters(), context);
                                    })
                            .findFirst();
        }
        FeatureEvaluationResult result = new FeatureEvaluationResult(enabled, null);
        if (enabledStrategy.isPresent()) {
            Strategy configuredStrategy = getStrategy(enabledStrategy.get().getName());
            result =
                    configuredStrategy.getResult(
                            enabledStrategy.get().getParameters(),
                            enhancedContext,
                            ConstraintMerger.mergeConstraints(
                                    featureRepository, enabledStrategy.get()),
                            enabledStrategy.get().getVariants());
        }

        Variant variant = result.isEnabled() ? result.getVariant() : null;
        // If strategy variant is null, look for a variant in the featureToggle
        if (variant == null && defaultVariant != null) {
            variant =
                    result.isEnabled()
                            ? VariantUtil.selectVariant(featureToggle, context, defaultVariant)
                            : defaultVariant;
        }
        result.setVariant(variant);

        return result;
    }

    private void checkIfToggleMatchesNamePrefix(String toggleName) {
        if (config.getNamePrefix() != null) {
            if (!toggleName.startsWith(config.getNamePrefix())) {
                LOGGER.warn(
                        "Toggle [{}] doesnt start with configured name prefix of [{}] so it will always be disabled",
                        toggleName,
                        config.getNamePrefix());
            }
        }
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, context, DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        FeatureEvaluationResult result =
                getFeatureEvaluationResult(toggleName, context, (n, c) -> false, defaultValue);
        Variant variant = result.getVariant();
        metricService.countVariant(toggleName, variant.getName());
        // Should count yes/no also when getting variant.
        metricService.count(toggleName, result.isEnabled());
        dispatchVariantImpressionDataIfNeeded(
                toggleName, variant.getName(), result.isEnabled(), context);
        return variant;
    }

    private void dispatchVariantImpressionDataIfNeeded(
            String toggleName, String variantName, boolean enabled, UnleashContext context) {
        FeatureToggle toggle = featureRepository.getToggle(toggleName);
        if (toggle != null && toggle.hasImpressionData()) {
            eventDispatcher.dispatch(
                    new VariantImpressionEvent(toggleName, enabled, context, variantName));
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

    /** Use more().count() instead */
    @Deprecated
    public void count(final String toggleName, boolean enabled) {
        metricService.count(toggleName, enabled);
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

    private Strategy getStrategy(String strategy) {
        return strategyMap.getOrDefault(strategy, config.getFallbackStrategy());
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
                                FeatureEvaluationResult result =
                                        getFeatureEvaluationResult(
                                                toggleName, context, (n, c) -> false, null);

                                return new EvaluatedToggle(
                                        toggleName, result.isEnabled(), result.getVariant());
                            })
                    .collect(Collectors.toList());
        }

        @Override
        public void count(final String toggleName, boolean enabled) {
            metricService.count(toggleName, enabled);
        }

        @Override
        public void countVariant(final String toggleName, String variantName) {
            metricService.countVariant(toggleName, variantName);
        }
    }
}
