package io.getunleash;

import static io.getunleash.Variant.DISABLED_VARIANT;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.getunleash.engine.*;
import io.getunleash.event.*;
import io.getunleash.lang.Nullable;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.metric.UnleashMetricServiceImpl;
import io.getunleash.repository.ClientFeaturesResponse;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.repository.IFeatureRepository;
import io.getunleash.strategy.*;
import io.getunleash.util.ConstraintMerger;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.VariantUtil;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
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

        this.unleashEngine = new UnleashEngine(strategyMap.values().stream().map(this::asIStrategy)
            .collect(Collectors.toList()), Optional.ofNullable(unleashConfig.getFallbackStrategy()).map(this::asIStrategy).orElse(null));
        unleashConfig.setUnleashEngine(unleashEngine);

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

    @NotNull
    private IStrategy asIStrategy(Strategy s) {
        return new IStrategy() {
            @Override
            public String getName() {
                return s.getName();
            }

            @Override
            public boolean isEnabled(Map<String, String> map, Context context) {
                return s.isEnabled(map, adapt(context));
            }
        };
    }

    private UnleashContext adapt(Context context) {
        ZonedDateTime currentTime = ZonedDateTime.now();
        if (context.getCurrentTime() != null) {
            try {
                currentTime = ZonedDateTime.parse(context.getCurrentTime());
            } catch (DateTimeParseException e){
                LOGGER.warn("Unable to parse current time from context: {}, using current time instead", context.getCurrentTime());
            }

        }

        return new UnleashContext(
            context.getAppName(),
            context.getEnvironment(),
            context.getUserId(),
            context.getSessionId(),
            context.getRemoteAddress(),
            currentTime,
            context.getProperties()
        ).applyStaticFields(config);
    }

    private Context adapt(UnleashContext context) {
        Context mapped = new Context();
        mapped.setAppName(context.getAppName().orElse(null));
        mapped.setEnvironment(context.getEnvironment().orElse(null));
        mapped.setUserId(context.getUserId().orElse(null));
        mapped.setSessionId(context.getSessionId().orElse(null));
        mapped.setRemoteAddress(context.getRemoteAddress().orElse(null));
        mapped.setProperties(context.getProperties());
        mapped.setCurrentTime(context.getCurrentTime().map(ZonedDateTime::toString).orElse(null));
        return mapped;
    }

    @Override
    public boolean isEnabled(final String toggleName, final boolean defaultSetting) {
        try {
            UnleashContext enhancedContext = contextProvider.getContext().applyStaticFields(config);
            return Optional.ofNullable(unleashEngine.isEnabled(toggleName, adapt(enhancedContext))).orElse(defaultSetting);
        } catch (YggdrasilInvalidInputException | YggdrasilError e) {
            throw new RuntimeException(e);
        }
        //return isEnabled(toggleName, contextProvider.getContext(), defaultSetting);
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
        return isEnabled(toggleName, context, fallbackAction, false);
    }

    public boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction,
            boolean isParent) {
        FeatureEvaluationResult result =
                getFeatureEvaluationResult(toggleName, context, fallbackAction, null);
        if (!isParent) {
            count(toggleName, result.isEnabled());
        }
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
        if (featureToggle == null) {
            return new FeatureEvaluationResult(
                    fallbackAction.test(toggleName, enhancedContext), defaultVariant);
        } else if (!featureToggle.isEnabled()) {
            return new FeatureEvaluationResult(false, defaultVariant);
        } else if (featureToggle.getStrategies().isEmpty()) {
            return new FeatureEvaluationResult(
                    true, VariantUtil.selectVariant(featureToggle, context, defaultVariant));
        } else {
            // Dependent toggles, no point in evaluating child strategies if our dependencies are
            // not satisfied
            if (isParentDependencySatisfied(featureToggle, context, fallbackAction)) {
                for (ActivationStrategy strategy : featureToggle.getStrategies()) {
                    Strategy configuredStrategy = getStrategy(strategy.getName());
                    if (configuredStrategy == UNKNOWN_STRATEGY) {
                        LOGGER.warn(
                                "Unable to find matching strategy for toggle:{} strategy:{}",
                                toggleName,
                                strategy.getName());
                    }

                    FeatureEvaluationResult result =
                            configuredStrategy.getResult(
                                    strategy.getParameters(),
                                    enhancedContext,
                                    ConstraintMerger.mergeConstraints(featureRepository, strategy),
                                    strategy.getVariants());

                    if (result.isEnabled()) {
                        Variant variant = result.getVariant();
                        // If strategy variant is null, look for a variant in the featureToggle
                        if (variant == null) {
                            variant =
                                    VariantUtil.selectVariant(
                                            featureToggle, context, defaultVariant);
                        }
                        result.setVariant(variant);
                        return result;
                    }
                }
            }
            return new FeatureEvaluationResult(false, defaultVariant);
        }
    }

    private boolean isParentDependencySatisfied(
            @Nonnull FeatureToggle featureToggle,
            @Nonnull UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction) {
        if (!featureToggle.hasDependencies()) {
            return true;
        } else {
            return featureToggle.getDependencies().stream()
                    .allMatch(
                            parent -> {
                                FeatureToggle parentToggle =
                                        featureRepository.getToggle(parent.getFeature());
                                if (parentToggle == null) {
                                    LOGGER.warn(
                                            "Missing dependency [{}] for toggle: [{}]",
                                            parent.getFeature(),
                                            featureToggle.getName());
                                    return false;
                                }
                                if (!parentToggle.getDependencies().isEmpty()) {
                                    LOGGER.warn(
                                            "[{}] depends on feature [{}] which also depends on something. We don't currently support more than one level of dependency resolution",
                                            featureToggle.getName(),
                                            parent.getFeature());
                                    return false;
                                }
                                boolean parentSatisfied =
                                        isEnabled(
                                                parent.getFeature(), context, fallbackAction, true);
                                if (parentSatisfied) {
                                    if (!parent.getVariants().isEmpty()) {
                                        return parent.getVariants()
                                                .contains(
                                                        getVariant(
                                                                        parent.feature,
                                                                        context,
                                                                        DISABLED_VARIANT,
                                                                        true)
                                                                .getName());
                                    } else {
                                        return parent.isEnabled();
                                    }
                                } else {
                                    return !parent.isEnabled();
                                }
                            });
        }
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
        return getVariant(toggleName, context, defaultValue, false);
    }

    private Variant getVariant(
            String toggleName, UnleashContext context, Variant defaultValue, boolean isParent) {
        FeatureEvaluationResult result =
                getFeatureEvaluationResult(toggleName, context, (n, c) -> false, defaultValue);
        Variant variant = result.getVariant();
        if (!isParent) {
            metricService.countVariant(toggleName, variant.getName());
            // Should count yes/no also when getting variant.
            metricService.count(toggleName, result.isEnabled());
        }
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
