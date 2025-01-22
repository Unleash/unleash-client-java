package io.getunleash;

import io.getunleash.event.EventDispatcher;
import io.getunleash.event.IsEnabledImpressionEvent;
import io.getunleash.event.ToggleEvaluated;
import io.getunleash.event.VariantImpressionEvent;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.strategy.*;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.Variant;

import static io.getunleash.variant.Variant.DISABLED_VARIANT;

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

    private final UnleashMetricService metricService;
    private final FeatureRepository featureRepository;
    private final UnleashContextProvider contextProvider;
    private final EventDispatcher eventDispatcher;
    private final UnleashConfig config;

    private static EngineProxy defaultToggleRepository(
            UnleashConfig unleashConfig, Strategy... strategies) {
        return new EngineProxyImpl(unleashConfig, strategies);
    }

    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        this(unleashConfig, defaultToggleRepository(unleashConfig, strategies));
    }

    public DefaultUnleash(UnleashConfig unleashConfig, EngineProxy engineProxy) {
        this(
                unleashConfig,
                engineProxy,
                unleashConfig.getContextProvider(),
                new EventDispatcher(unleashConfig),
                false);
    }

    // Visible for testing
    public DefaultUnleash(
            UnleashConfig unleashConfig,
            EngineProxy engineProxy,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher) {
        this(unleashConfig, engineProxy, contextProvider, eventDispatcher, false);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig,
            EngineProxy engineProxy,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher,
            boolean failOnMultipleInstantiations) {

        this.config = unleashConfig;
        this.featureRepository = engineProxy;
        this.metricService = engineProxy;
        this.contextProvider = contextProvider;
        this.eventDispatcher = eventDispatcher;
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

        Boolean enabled = this.featureRepository.isEnabled(toggleName, enhancedContext);
        if (enabled == null) {
            enabled = fallbackAction.test(toggleName, enhancedContext);
        }

        this.metricService.countToggle(toggleName, enabled);
        eventDispatcher.dispatch(new ToggleEvaluated(toggleName, enabled));
        if (this.featureRepository.shouldEmitImpressionEvent(toggleName)) {
            eventDispatcher.dispatch(new IsEnabledImpressionEvent(toggleName, enabled, context));
        }
        return enabled;
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, context, DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName) {
        return getVariant(toggleName, contextProvider.getContext());
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        return getVariant(toggleName, contextProvider.getContext(), defaultValue);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        UnleashContext enhancedContext = context.applyStaticFields(config);

        Variant variant =
                this.featureRepository.getVariant(toggleName, enhancedContext, defaultValue);
        this.metricService.countToggle(toggleName, variant.isFeatureEnabled());
        this.metricService.countVariant(toggleName, variant.getName());
        eventDispatcher.dispatch(new ToggleEvaluated(toggleName, variant.isEnabled()));
        if (this.featureRepository.shouldEmitImpressionEvent(toggleName)) {
            eventDispatcher.dispatch(
                    new VariantImpressionEvent(
                            toggleName, variant.isFeatureEnabled(), context, variant.getName()));
        }
        return variant;
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
            return featureRepository
                    .listKnownToggles()
                    .map(FeatureDefinition::getName)
                    .collect(Collectors.toList());
        }

        @Override
        public Optional<FeatureDefinition> getFeatureToggleDefinition(String toggleName) {
            return featureRepository
                    .listKnownToggles()
                    .filter(toggle -> toggle.getName().equals(toggleName))
                    .findFirst();
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles() {
            return evaluateAllToggles(contextProvider.getContext());
        }

        @Override
        public List<EvaluatedToggle> evaluateAllToggles(UnleashContext context) {
            return featureRepository
                    .listKnownToggles()
                    .map(FeatureDefinition::getName)
                    .map(
                            toggleName -> {
                                UnleashContext enhancedContext = context.applyStaticFields(config);
                                Variant variant =
                                        featureRepository.getVariant(
                                                toggleName, enhancedContext, DISABLED_VARIANT);
                                return new EvaluatedToggle(
                                        toggleName, variant.isFeatureEnabled(), variant);
                            })
                    .collect(Collectors.toList());
        }
    }
}
