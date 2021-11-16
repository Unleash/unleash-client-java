package io.getunleash;

import static io.getunleash.Variant.DISABLED_VARIANT;
import static java.util.Optional.ofNullable;

import io.getunleash.event.EventDispatcher;
import io.getunleash.event.ToggleEvaluated;
import io.getunleash.lang.Nullable;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.metric.UnleashMetricServiceImpl;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.repository.FeatureToggleRepository;
import io.getunleash.repository.ToggleBackupHandlerFile;
import io.getunleash.repository.ToggleRepository;
import io.getunleash.strategy.*;
import io.getunleash.util.ConstraintMerger;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.VariantUtil;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultUnleash implements Unleash {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUnleash.class);
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
    private final FeatureRepository featureRepository;
    private final Map<String, Strategy> strategyMap;
    private final UnleashContextProvider contextProvider;
    private final EventDispatcher eventDispatcher;
    private final UnleashConfig config;

    private static FeatureRepository defaultToggleRepository(UnleashConfig unleashConfig) {
        return new FeatureRepository(unleashConfig);
    }

    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        this(unleashConfig, defaultToggleRepository(unleashConfig), strategies);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig,
            FeatureRepository featureRepository,
            Strategy... strategies) {
        this(
                unleashConfig,
            featureRepository,
                buildStrategyMap(strategies),
                unleashConfig.getContextProvider(),
                new EventDispatcher(unleashConfig),
                new UnleashMetricServiceImpl(unleashConfig, unleashConfig.getScheduledExecutor()));
    }

    // Visible for testing
    public DefaultUnleash(
            UnleashConfig unleashConfig,
            FeatureRepository featureRepository,
            Map<String, Strategy> strategyMap,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher,
            UnleashMetricService metricService) {
        this.config = unleashConfig;
        this.featureRepository = featureRepository;
        this.strategyMap = strategyMap;
        this.contextProvider = contextProvider;
        this.eventDispatcher = eventDispatcher;
        this.metricService = metricService;
        metricService.register(strategyMap.keySet());
    }

    @Override
    public boolean isEnabled(final String toggleName) {
        return isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(final String toggleName, final boolean defaultSetting) {
        return isEnabled(toggleName, contextProvider.getContext(), defaultSetting);
    }

    @Override
    public boolean isEnabled(
            final String toggleName, final UnleashContext context, final boolean defaultSetting) {
        return isEnabled(toggleName, context, (n, c) -> defaultSetting);
    }

    @Override
    public boolean isEnabled(
            final String toggleName,
            final BiFunction<String, UnleashContext, Boolean> fallbackAction) {
        return isEnabled(toggleName, contextProvider.getContext(), fallbackAction);
    }

    @Override
    public boolean isEnabled(
            String toggleName,
            UnleashContext context,
            BiFunction<String, UnleashContext, Boolean> fallbackAction) {
        boolean enabled = checkEnabled(toggleName, context, fallbackAction);
        count(toggleName, enabled);
        eventDispatcher.dispatch(new ToggleEvaluated(toggleName, enabled));
        return enabled;
    }

    private boolean checkEnabled(
            String toggleName,
            UnleashContext context,
            BiFunction<String, UnleashContext, Boolean> fallbackAction) {
        checkIfToggleMatchesNamePrefix(toggleName);
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        boolean enabled;
        UnleashContext enhancedContext = context.applyStaticFields(config);

        if (featureToggle == null) {
            enabled = fallbackAction.apply(toggleName, enhancedContext);
        } else if (!featureToggle.isEnabled()) {
            enabled = false;
        } else if (featureToggle.getStrategies().size() == 0) {
            return true;
        } else {
            enabled =
                    featureToggle.getStrategies().stream()
                            .anyMatch(
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
                                                strategy.getParameters(),
                                                enhancedContext,
                                                ConstraintMerger.mergeConstraints(
                                                        featureRepository, strategy));
                                    });
        }
        return enabled;
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
        FeatureToggle featureToggle = featureRepository.getToggle(toggleName);
        boolean enabled = checkEnabled(toggleName, context, (n, c) -> false);
        Variant variant =
                enabled
                        ? VariantUtil.selectVariant(featureToggle, context, defaultValue)
                        : defaultValue;
        metricService.countVariant(toggleName, variant.getName());
        return variant;
    }

    @Override
    public Variant getVariant(String toggleName) {
        return getVariant(toggleName, contextProvider.getContext());
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        return getVariant(toggleName, contextProvider.getContext(), defaultValue);
    }

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
        public List<EvaluatedToggle> evaluateAllToggles() {
            return evaluateAllToggles(contextProvider.getContext());
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles(UnleashContext context) {
            return getFeatureToggleNames().stream()
                    .map(
                            toggleName -> {
                                boolean enabled =
                                        checkEnabled(toggleName, context, (n, c) -> false);
                                FeatureToggle featureToggle =
                                        featureRepository.getToggle(toggleName);
                                Variant variant =
                                        enabled
                                                ? VariantUtil.selectVariant(
                                                        featureToggle, context, DISABLED_VARIANT)
                                                : DISABLED_VARIANT;

                                return new EvaluatedToggle(toggleName, enabled, variant);
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
