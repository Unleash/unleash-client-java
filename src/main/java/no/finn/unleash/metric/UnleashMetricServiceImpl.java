package no.finn.unleash.metric;

import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

public class UnleashMetricServiceImpl implements UnleashMetricService {
    private final LocalDateTime started;
    private final UnleashConfig unleashConfig;
    private final long metricsInterval;
    private final UnleashMetricsSender unleashMetricsSender;

    //mutable
    private MetricsBucket currentMetricsBucket;

    public UnleashMetricServiceImpl(UnleashConfig unleashConfig, UnleashScheduledExecutor executor) {
        this(unleashConfig, new UnleashMetricsSender(unleashConfig), executor);
    }

    public UnleashMetricServiceImpl(UnleashConfig unleashConfig,
                                    UnleashMetricsSender unleashMetricsSender,
                                    UnleashScheduledExecutor executor) {
        this.currentMetricsBucket = new MetricsBucket();
        this.started = LocalDateTime.now(ZoneId.of("UTC"));
        this.unleashConfig = unleashConfig;
        this.metricsInterval = unleashConfig.getSendMetricsInterval();
        this.unleashMetricsSender = unleashMetricsSender;

        executor.setInterval(sendMetrics(), metricsInterval, metricsInterval);
    }

    @Override
    public void register(Set<String> strategies) {
        ClientRegistration registration = new ClientRegistration(unleashConfig, started, strategies);
        unleashMetricsSender.registerClient(registration);
    }

    @Override
    public void count(String toggleName, boolean active) {
        currentMetricsBucket.registerCount(toggleName, active);
    }

    @Override
    public void countVariant(String toggleName, String variantName) {
        //TODO Implement this
    }

    private Runnable sendMetrics() {
        return () -> {
            MetricsBucket metricsBucket = this.currentMetricsBucket;
            this.currentMetricsBucket = new MetricsBucket();
            metricsBucket.end();
            ClientMetrics metrics = new ClientMetrics(unleashConfig, metricsBucket);
            unleashMetricsSender.sendMetrics(metrics);
        };
    }
}
