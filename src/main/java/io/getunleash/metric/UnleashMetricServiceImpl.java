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

    private final int maxSkips;
    private final AtomicInteger failures = new AtomicInteger();
    private final AtomicInteger skips = new AtomicInteger();

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
        this.maxSkips =
                Integer.max(20, 300 / Integer.max((int) unleashConfig.getSendMetricsInterval(), 1));
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
            if (skips.get() == 0) {
                MetricsBucket metricsBucket = this.currentMetricsBucket;
                this.currentMetricsBucket = new MetricsBucket();
                metricsBucket.end();
                ClientMetrics metrics = new ClientMetrics(unleashConfig, metricsBucket);
                int statusCode = metricSender.sendMetrics(metrics);
                if (statusCode >= 200 && statusCode < 400) {
                    decrementFailureCountAndResetSkips();
                }
                if (statusCode >= 400) {
                    handleHttpErrorCodes(statusCode);
                }
            } else {
                skips.decrementAndGet();
            }
        };
    }

    /**
     * We've had one successful call, so if we had 10 failures in a row, this will reduce the skips
     * down to 9, so that we gradually start polling more often, instead of doing max load
     * immediately after a sequence of errors.
     */
    private void decrementFailureCountAndResetSkips() {
        skips.set(Math.max(failures.decrementAndGet(), 0));
    }

    /**
     * We've gotten the message to back off (usually a 429 or a 50x). If we have successive
     * failures, failure count here will be incremented higher and higher which will handle
     * increasing our backoff, since we set the skip count to the failure count after every reset
     */
    private void increaseSkipCount() {
        skips.set(Math.min(failures.incrementAndGet(), maxSkips));
    }

    /**
     * We've received an error code that we don't expect to change, which means we've already logged
     * an ERROR. To avoid hammering the server that just told us we did something wrong and to avoid
     * flooding the logs, we'll increase our skip count to maximum
     */
    private void maximizeSkips() {
        skips.set(maxSkips);
        failures.incrementAndGet();
    }

    private void handleHttpErrorCodes(int responseCode) {
        if (responseCode == 404) {
            maximizeSkips();
            LOGGER.error(
                    "Server said that the Metrics receiving endpoint at {} does not exist. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashURLs().getClientMetricsURL(),
                    maxSkips);
        } else if (responseCode == 429) {
            increaseSkipCount();
            LOGGER.info(
                    "Client Metrics was RATE LIMITED for the {}. time. Further backing off. Current backoff at {} times our metrics post interval",
                    failures.get(),
                    skips.get());
        } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            maximizeSkips();
            LOGGER.error(
                    "Client was not authorized to post metrics to the Unleash API at {}. Backing off to {} times our poll interval to avoid overloading server",
                    unleashConfig.getUnleashURLs().getClientMetricsURL(),
                    maxSkips);
        } else if (responseCode >= 500) {
            increaseSkipCount();
            LOGGER.info(
                    "Server failed with a {} status code. Backing off. Current backoff at {} times our poll interval",
                    responseCode,
                    skips.get());
        }
    }

    protected int getSkips() {
        return this.skips.get();
    }

    protected int getFailures() {
        return this.failures.get();
    }
}
