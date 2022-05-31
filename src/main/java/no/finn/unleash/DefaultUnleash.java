package no.finn.unleash;

import io.getunleash.UnleashContextProvider;
import io.getunleash.event.EventDispatcher;
import io.getunleash.metric.UnleashMetricService;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.strategy.Strategy;
import io.getunleash.util.UnleashConfig;
import java.util.Map;

/**
 * @deprecated Use {@link io.getunleash.DefaultUnleash} (since v5.0.0)
 */
@Deprecated
public class DefaultUnleash extends io.getunleash.DefaultUnleash {
    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        super(unleashConfig, strategies);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig, FeatureRepository repository, Strategy... strategies) {
        super(unleashConfig, repository, strategies);
    }

    public DefaultUnleash(
            UnleashConfig unleashConfig,
            FeatureRepository repository,
            Map<String, Strategy> strategyMap,
            UnleashContextProvider contextProvider,
            EventDispatcher eventDispatcher,
            UnleashMetricService metricService) {
        super(
                unleashConfig,
                repository,
                strategyMap,
                contextProvider,
                eventDispatcher,
                metricService);
    }
}
