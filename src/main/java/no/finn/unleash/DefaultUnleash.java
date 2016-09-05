package no.finn.unleash;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.ToggleBackupHandlerFile;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.DefaultStrategy;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.UnknownStrategy;

public final class DefaultUnleash implements Unleash {
    private static final DefaultStrategy DEFAULT_STRATEGY = new DefaultStrategy();
    private static final UnknownStrategy UNKNOWN_STRATEGY = new UnknownStrategy();

    private final ToggleRepository toggleRepository;
    private final Map<String, Strategy> strategyMap;

    public DefaultUnleash(URI unleashServer, Strategy... strategies) {
        this(new FeatureToggleRepository(
                        new HttpToggleFetcher(unleashServer),
                        new ToggleBackupHandlerFile()),
                strategies);
    }

    public DefaultUnleash(ToggleRepository toggleRepository, Strategy... strategies) {
        this.toggleRepository = toggleRepository;
        this.strategyMap = buildStrategyMap(strategies);
    }

    @Override
    public boolean isEnabled(final String toggleName) {
        return isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(final String toggleName, final boolean defaultSetting) {
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);

        if (featureToggle == null) {
            return defaultSetting;
        } else if(!featureToggle.isEnabled()) {
            return false;
        }

        return featureToggle.getStrategies().stream()
                .filter(as -> getStrategy(as.getName()).isEnabled(as.getParameters()))
                .findFirst()
                .isPresent();
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
