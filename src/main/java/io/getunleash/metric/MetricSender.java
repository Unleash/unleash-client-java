package io.getunleash.metric;

public interface MetricSender {
    void registerClient(ClientRegistration registration);

    void sendMetrics(ClientMetrics metrics);
}
