package io.getunleash.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.getunleash.FeatureToggle;
import io.getunleash.lang.Nullable;

public final class ToggleCollection {
    private final Collection<FeatureToggle> features;
    private final int version = 1; // required for serialization
    private final transient Map<String, FeatureToggle> cache;

    public ToggleCollection(final Collection<FeatureToggle> features) {
        this.features = ensureNotNull(features);
        this.cache = new ConcurrentHashMap<>();
        for (FeatureToggle featureToggle : this.features) {
            cache.put(featureToggle.getName(), featureToggle);
        }
    }

    private Collection<FeatureToggle> ensureNotNull(@Nullable Collection<FeatureToggle> features) {
        if (features == null) {
            return Collections.emptyList();
        }
        return features;
    }

    public Collection<FeatureToggle> getFeatures() {
        return Collections.unmodifiableCollection(features);
    }

    public FeatureToggle getToggle(final String name) {
        return cache.get(name);
    }
}
