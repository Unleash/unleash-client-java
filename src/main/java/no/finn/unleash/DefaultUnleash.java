package no.finn.unleash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.metric.UnleashMetricService;
import no.finn.unleash.metric.UnleashMetricServiceImpl;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.ToggleBackupHandlerFile;
import no.finn.unleash.repository.HttpToggleFetcher;
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
import no.finn.unleash.util.UnleashScheduledExecutor;
import no.finn.unleash.util.UnleashScheduledExecutorImpl;

public final class DefaultUnleash implements Unleash {
    private static final List<Strategy> BUILTIN_STRATEGIES = Arrays.asList(new DefaultStrategy(),
            new ApplicationHostnameStrategy(),
            new GradualRolloutRandomStrategy(),
            new GradualRolloutSessionIdStrategy(),
            new GradualRolloutUserIdStrategy(),
            new RemoteAddressStrategy(),
            new UserWithIdStrategy());

    private static final UnknownStrategy UNKNOWN_STRATEGY = new UnknownStrategy();
    private static final UnleashScheduledExecutor unleashScheduledExecutor = new UnleashScheduledExecutorImpl();

    private final UnleashMetricService metricService;
    private final ToggleRepository toggleRepository;
    private final Map<String, Strategy> strategyMap;
    private final UnleashContextProvider contextProvider;


    private static FeatureToggleRepository defaultToggleRepository(UnleashConfig unleashConfig) {
        return new FeatureToggleRepository(
                unleashConfig,
                unleashScheduledExecutor,
                new HttpToggleFetcher(unleashConfig),
                new ToggleBackupHandlerFile(unleashConfig));
    }

    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        this(unleashConfig, defaultToggleRepository(unleashConfig), strategies);
    }

    public DefaultUnleash(UnleashConfig unleashConfig, ToggleRepository toggleRepository, Strategy... strategies) {
        this.toggleRepository = toggleRepository;
        this.strategyMap = buildStrategyMap(strategies);
        this.contextProvider = unleashConfig.getContextProvider();
        this.metricService = new UnleashMetricServiceImpl(unleashConfig, unleashScheduledExecutor);
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
        final boolean enabled;
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);

        if (featureToggle == null) {
            enabled = defaultSetting;
        } else if(!featureToggle.isEnabled()) {
            enabled = false;
        } else if(featureToggle.getStrategies().size() == 0) {
            return true;
        } else {
            enabled = resolveStrategies("OR", featureToggle.getStrategies(), context);
        }

        count(toggleName, enabled);
        return enabled;
    }

    private boolean resolveStrategies(String operator, List<ActivationStrategy> strategies, UnleashContext context) {
        switch (operator) {
            case "AND":
                return strategies.stream().allMatch(as -> resolveStrategy(as, context));
            case "OR":
            default:
                return strategies.stream().anyMatch(as -> resolveStrategy(as, context));
        }
    }

    private boolean resolveStrategy(ActivationStrategy as, UnleashContext context) {
        if(as.getName().equals("__internal-strategy-group")) {
            return resolveStrategies(as.getOperator(), as.getGroup(), context);
        } else {
            return getStrategy(as.getName()).isEnabled(as.getParameters(), context);
        }
    }

    public Optional<FeatureToggle> getFeatureToggleDefinition(String toggleName) {
        return Optional.ofNullable(toggleRepository.getToggle(toggleName));
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
