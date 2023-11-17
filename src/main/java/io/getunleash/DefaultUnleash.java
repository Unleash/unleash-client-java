package io.getunleash;

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
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static io.getunleash.Variant.DISABLED_VARIANT;
import static java.util.Optional.ofNullable;

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

        this.unleashEngine = new UnleashEngine(
            strategyMap.values().stream().map(this::asIStrategy).collect(Collectors.toList()),
            Optional.ofNullable(unleashConfig.getFallbackStrategy()).map(this::asIStrategy).orElse(null)
        );
        featureRepository.addConsumer(featureCollection -> {
            try {
                this.unleashEngine.takeState(JsonFeatureParser.toJsonString(featureCollection));
            } catch (YggdrasilInvalidInputException e) {
                LOGGER.error("Unable to update features", e);
            }
        });

        this.config = unleashConfig;
        this.featureRepository = featureRepository;
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
        mapped.setCurrentTime(DateTimeFormatter.ISO_DATE_TIME.format(context.getCurrentTime().orElse(ZonedDateTime.now())));
        return mapped;
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
        dispatchEnabledImpressionDataIfNeeded(toggleName, result.isEnabled(), context);
        return result.isEnabled();
    }

    private void dispatchEnabledImpressionDataIfNeeded(
            String toggleName, boolean enabled, UnleashContext context) {
        try {
            if (this.unleashEngine.shouldEmitImpressionEvent(toggleName)) {
                eventDispatcher.dispatch(new IsEnabledImpressionEvent(toggleName, enabled, context));
            }
        } catch (YggdrasilError e) {
            LOGGER.warn("Unable to check if impression event should be emitted", e);
        }
    }

    private FeatureEvaluationResult getFeatureEvaluationResult(
            String toggleName,
            UnleashContext context,
            BiPredicate<String, UnleashContext> fallbackAction,
            @Nullable Variant defaultVariant) {
        UnleashContext enhancedContext = context.applyStaticFields(config);
        try {
            VariantDef variantResponse = this.unleashEngine.getVariant(toggleName, adapt(enhancedContext));
            if (variantResponse != null) {
                return new FeatureEvaluationResult(variantResponse.isEnabled(),
                    new Variant(variantResponse.getName(), adapt(variantResponse.getPayload()), variantResponse.isEnabled()));
            }

            Boolean isEnabled = this.unleashEngine.isEnabled(toggleName, adapt(enhancedContext));
            if (isEnabled != null) {
                return new FeatureEvaluationResult(isEnabled, defaultVariant);
            }

            return new FeatureEvaluationResult(fallbackAction.test(toggleName, enhancedContext), defaultVariant);

        } catch (YggdrasilInvalidInputException | YggdrasilError e) {
            throw new RuntimeException(e);
        }
    }

    private @Nullable io.getunleash.variant.Payload adapt(@Nullable Payload payload) {
        return Optional.ofNullable(payload).map(p ->
            new io.getunleash.variant.Payload(p.getType(), p.getValue())
        ).orElse(null);
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
        try {
            if (unleashEngine.shouldEmitImpressionEvent(toggleName)) {
                eventDispatcher.dispatch(
                        new VariantImpressionEvent(toggleName, enabled, context, variantName));
            }
        } catch (YggdrasilError e) {
            LOGGER.warn("Unable to check if impression event should be emitted", e);
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
