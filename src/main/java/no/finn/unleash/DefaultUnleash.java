package no.finn.unleash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.event.EventDispatcher;
import no.finn.unleash.event.ToggleEvaluated;
import no.finn.unleash.metric.UnleashMetricService;
import no.finn.unleash.metric.UnleashMetricServiceImpl;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleBackupHandlerFile;
import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.ApplicationHostnameStrategy;
import no.finn.unleash.strategy.DefaultStrategy;
import no.finn.unleash.strategy.GradualRolloutRandomStrategy;
import no.finn.unleash.strategy.GradualRolloutSessionIdStrategy;
import no.finn.unleash.strategy.GradualRolloutUserIdStrategy;
import no.finn.unleash.strategy.RemoteAddressStrategy;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.UnknownStrategy;
import no.finn.unleash.strategy.UserWithIdStrategy;
import no.finn.unleash.util.UnleashConfig;

import static java.util.Optional.ofNullable;
import static no.finn.unleash.Variant.DISABLED_VARIANT;
import static no.finn.unleash.variant.VariantUtil.selectVariant;

public final class DefaultUnleash implements Unleash {
    private static final List<Strategy> BUILTIN_STRATEGIES = Arrays.asList(new DefaultStrategy(),
            new ApplicationHostnameStrategy(),
            new GradualRolloutRandomStrategy(),
            new GradualRolloutSessionIdStrategy(),
            new GradualRolloutUserIdStrategy(),
            new RemoteAddressStrategy(),
            new UserWithIdStrategy());

    private static final UnknownStrategy UNKNOWN_STRATEGY = new UnknownStrategy();

    private final UnleashMetricService metricService;
    private final ToggleRepository toggleRepository;
    private final Map<String, Strategy> strategyMap;
    private final UnleashContextProvider contextProvider;
    private final EventDispatcher eventDispatcher;


    private static FeatureToggleRepository defaultToggleRepository(UnleashConfig unleashConfig) {
        return new FeatureToggleRepository(
                unleashConfig,
                new HttpToggleFetcher(unleashConfig),
                new ToggleBackupHandlerFile(unleashConfig)
        );
    }

    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        this(unleashConfig, defaultToggleRepository(unleashConfig), strategies);
    }

    public DefaultUnleash(UnleashConfig unleashConfig, ToggleRepository toggleRepository, Strategy... strategies) {
        this.toggleRepository = toggleRepository;
        this.strategyMap = buildStrategyMap(strategies);
        this.contextProvider = unleashConfig.getContextProvider();
        this.eventDispatcher = new EventDispatcher(unleashConfig);
        this.metricService = new UnleashMetricServiceImpl(unleashConfig, unleashConfig.getScheduledExecutor());
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
    public boolean isEnabled(final String toggleName, final UnleashContext context ,final boolean defaultSetting) {
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        boolean enabled = isEnabled(featureToggle, context, defaultSetting);
        count(toggleName, enabled);
        eventDispatcher.dispatch(new ToggleEvaluated(toggleName,enabled));
        return enabled;
    }

    private boolean isEnabled(FeatureToggle featureToggle, UnleashContext context, boolean defaultSetting) {
        boolean enabled;
        if (featureToggle == null) {
            enabled = defaultSetting;
        } else if(!featureToggle.isEnabled()) {
            enabled = false;
        } else if(featureToggle.getStrategies().size() == 0) {
            return true;
        } else {
            enabled = featureToggle.getStrategies().stream()
                    .anyMatch(as -> getStrategy(as.getName()).isEnabled(as.getParameters(), context));
        }
        return enabled;
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, context, DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        boolean enabled = isEnabled(featureToggle, context, false);
        Variant variant = enabled ? selectVariant(featureToggle, context, defaultValue) : defaultValue;
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
        return ofNullable(toggleRepository.getToggle(toggleName));
    }

    public List<String> getFeatureToggleNames() {
        return toggleRepository.getFeatureNames();
    }

    public void count(final String toggleName, boolean enabled) {
        metricService.count(toggleName, enabled);
    }

    private Map<String, Strategy> buildStrategyMap(Strategy[] strategies) {
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
        return strategyMap.containsKey(strategy) ? strategyMap.get(strategy) : UNKNOWN_STRATEGY;
    }
}
