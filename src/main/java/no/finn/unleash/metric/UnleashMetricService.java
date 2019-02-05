package no.finn.unleash.metric;

import java.util.Set;

public interface UnleashMetricService {
    void register(Set<String> strategies);
    void count(String toggleName, boolean active);
    void countVariant(String toggleName, String variantName);
}
