package io.getunleash.metric;

import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

public class UnleashMetricServiceImpl implements UnleashMetricService {
    private final LocalDateTime started;
    private final UnleashConfig unleashConfig;

    private final MetricSender metricSender;

    // mutable
    private volatile MetricsBucket currentMetricsBucket;

    public UnleashMetricServiceImpl(
            UnleashConfig unleashConfig, UnleashScheduledExecutor executor) {
        this(unleashConfig, unleashConfig.getMetricSenderFactory().apply(unleashConfig), executor);
    }

    public UnleashMetricServiceImpl(
            UnleashConfig unleashConfig,
            MetricSender metricSender,
            UnleashScheduledExecutor executor) {
        this.currentMetricsBucket = new MetricsBucket();
        this.started = LocalDateTime.now(ZoneId.of("UTC"));
        this.unleashConfig = unleashConfig;
        this.metricSender = metricSender;
        long metricsInterval = unleashConfig.getSendMetricsInterval();
        executor.setInterval(sendMetrics(), metricsInterval, metricsInterval);
    }

    @Override
    public void register(Set<String> strategies) {
        ClientRegistration registration =
                new ClientRegistration(unleashConfig, started, strategies);
        metricSender.registerClient(registration);
    }

    @Override
    public void count(String toggleName, boolean active) {
        currentMetricsBucket.registerCount(toggleName, active);
    }

    @Override
    public void countVariant(String toggleName, String variantName) {
        currentMetricsBucket.registerCount(toggleName, variantName);
    }

    private Runnable sendMetrics() {
        return () -> {
            MetricsBucket metricsBucket = this.currentMetricsBucket;
            this.currentMetricsBucket = new MetricsBucket();
            metricsBucket.end();
            ClientMetrics metrics = new ClientMetrics(unleashConfig, metricsBucket);
            metricSender.sendMetrics(metrics);
        };
    }
}
