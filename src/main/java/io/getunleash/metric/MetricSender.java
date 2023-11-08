package io.getunleash.metric;

public interface MetricSender {
    int registerClient(ClientRegistration registration);

    int sendMetrics(ClientMetrics metrics);
}
