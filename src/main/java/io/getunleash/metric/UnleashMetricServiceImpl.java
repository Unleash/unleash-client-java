package io.getunleash.metric;

import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnleashMetricServiceImpl implements UnleashMetricService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnleashMetricServiceImpl.class);
    private final LocalDateTime started;
    private final UnleashConfig unleashConfig;

    private final MetricSender metricSender;

    // mutable
    private volatile MetricsBucket currentMetricsBucket;

    private final int maxInterval;
    private final AtomicInteger failures = new AtomicInteger();
    private final AtomicInteger interval = new AtomicInteger();

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
        this.maxInterval =
                Integer.max(
                        20,
                        300
                                / Integer.max(
                                        Long.valueOf(unleashConfig.getSendMetricsInterval())
                                                .intValue(),
                                        1));
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
            if (interval.get() == 0) {
                MetricsBucket metricsBucket = this.currentMetricsBucket;
                this.currentMetricsBucket = new MetricsBucket();
                metricsBucket.end();
                ClientMetrics metrics = new ClientMetrics(unleashConfig, metricsBucket);
                int statusCode = metricSender.sendMetrics(metrics);
                if (statusCode >= 200 && statusCode < 400) {
                    if (failures.get() > 0) {
                        interval.set(Integer.max(failures.decrementAndGet(), 0));
                    }
                }
                if (statusCode >= 400) {
                    handleHttpErrorCodes(statusCode);
                }
            } else {
                interval.decrementAndGet();
            }
        };
    }

    private void handleHttpErrorCodes(int responseCode) {
        if (responseCode == 404) {
            interval.set(maxInterval);
            failures.incrementAndGet();
            LOGGER.error(
                    "Server said that the Metrics receiving endpoint at {} does not exist. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashURLs().getClientMetricsURL(),
                    maxInterval);
        } else if (responseCode == 429) {
            interval.set(Math.min(failures.incrementAndGet(), maxInterval));
            LOGGER.info(
                    "Client Metrics was RATE LIMITED for the {}. time. Further backing off. Current backoff at {} times our metrics post interval",
                    failures.get(),
                    interval.get());
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            failures.incrementAndGet();
            interval.set(maxInterval);
            LOGGER.error(
                    "Client was not authorized to post metrics to the Unleash API at {}. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashURLs().getClientMetricsURL(),
                    maxInterval);
        } else if (responseCode >= 500) {
            interval.set(Math.min(failures.incrementAndGet(), maxInterval));
            LOGGER.info(
                    "Server failed with a {} status code. Backing off. Current backoff at {} times our poll interval",
                    responseCode,
                    interval.get());
        }
    }

    protected int getInterval() {
        return this.interval.get();
    }

    protected int getFailures() {
        return this.failures.get();
    }
}
