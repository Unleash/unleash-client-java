package no.finn.unleash;

import java.util.HashMap;
import java.util.Map;

import no.finn.unleash.metric.UnleashMetricService;
import no.finn.unleash.metric.UnleashMetricServiceImpl;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.ToggleBackupHandlerFile;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.DefaultStrategy;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.UnknownStrategy;
import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import no.finn.unleash.util.UnleashScheduledExecutorImpl;

public final class DefaultUnleash implements Unleash {
    private static final DefaultStrategy DEFAULT_STRATEGY = new DefaultStrategy();
    private static final UnknownStrategy UNKNOWN_STRATEGY = new UnknownStrategy();
    private static final UnleashScheduledExecutor unleashScheduledExecutor = new UnleashScheduledExecutorImpl();

    private final UnleashMetricService metricService;
    private final ToggleRepository toggleRepository;
    private final Map<String, Strategy> strategyMap;


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
        this.metricService = new UnleashMetricServiceImpl(unleashConfig, unleashScheduledExecutor);
        metricService.register(strategyMap.keySet());
    }

    @Override
    public boolean isEnabled(final String toggleName) {
        return isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(final String toggleName, final boolean defaultSetting) {
        boolean enabled = false;
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);

        if (featureToggle == null) {
            enabled = defaultSetting;
        } else if(!featureToggle.isEnabled()) {
            enabled = false;
        } else {
            enabled = featureToggle.getStrategies().stream()
                    .filter(as -> getStrategy(as.getName()).isEnabled(as.getParameters()))
                    .findFirst()
                    .isPresent();
        }

        metricService.count(toggleName, enabled);
        return enabled;
    }

    private Map<String, Strategy> buildStrategyMap(Strategy[] strategies) {
        Map<String, Strategy> map = new HashMap<>();

        map.put(DEFAULT_STRATEGY.getName(), DEFAULT_STRATEGY);

        if (strategies != null) {
            for (Strategy strategy : strategies) {
                map.put(strategy.getName(), strategy);
            }
        }

        return map;
    }


    private Strategy getStrategy(String strategy) {
        if (strategyMap.containsKey(strategy)) {
            return strategyMap.get(strategy);
        } else {
            return UNKNOWN_STRATEGY;
        }
    }
}
