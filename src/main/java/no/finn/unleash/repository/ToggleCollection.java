package no.finn.unleash.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import no.finn.unleash.FeatureToggle;

final class ToggleCollection {
    private final Collection<FeatureToggle> features;
    private final int version = 1;
    private final transient Map<String, FeatureToggle> cache;

    ToggleCollection(final Collection<FeatureToggle> features) {
        this.features = ensureNotNull(features);
        this.cache = new HashMap<>();
        for(FeatureToggle featureToggle : this.features) {
            cache.put(featureToggle.getName(), featureToggle);
        }
    }

    private Collection<FeatureToggle> ensureNotNull(Collection<FeatureToggle> features) {
        if (features == null) { return Collections.emptyList(); }
        return features;
    }

    Collection<FeatureToggle> getFeatures() {
        return features;
    }

    FeatureToggle getToggle(final String name) {
        return cache.get(name);
    }
}
