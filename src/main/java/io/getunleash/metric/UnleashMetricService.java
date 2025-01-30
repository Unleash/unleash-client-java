package io.getunleash.metric;

import java.util.Set;

public interface UnleashMetricService {
    void register(Set<String> strategies);

    void countToggle(String name, boolean enabled);

    void countVariant(String name, String variantName);
}
