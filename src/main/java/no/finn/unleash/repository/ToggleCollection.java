package no.finn.unleash.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import no.finn.unleash.FeatureToggle;

public final class ToggleCollection {
    private final Collection<FeatureToggle> features;
    private final int version = 1; // required for serialization
    private final transient Map<String, FeatureToggle> cache;

    ToggleCollection(final Collection<FeatureToggle> features) {
        this.features = ensureNotNull(features);
        this.cache = new ConcurrentHashMap<>();
        for(FeatureToggle featureToggle : this.features) {
            cache.put(featureToggle.getName(), featureToggle);
        }
    }

    private Collection<FeatureToggle> ensureNotNull(Collection<FeatureToggle> features) {
        if (features == null) { return Collections.emptyList(); }
        return features;
    }

    Collection<FeatureToggle> getFeatures() {
        return Collections.unmodifiableCollection(features);
    }

    FeatureToggle getToggle(final String name) {
        return cache.get(name);
    }
}
