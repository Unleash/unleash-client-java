package no.finn.unleash.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.finn.unleash.FeatureToggle;

final class ToggleCollection {
    private Collection<FeatureToggle> features = Collections.emptyList();
    private transient Map<String, FeatureToggle> cache;

    ToggleCollection(final Collection<FeatureToggle> features) {
        this.features = features;
        cache = new HashMap<>();
        for(FeatureToggle featureToggle : features) {
            cache.put(featureToggle.getName(), featureToggle);
        }
    }

    Collection<FeatureToggle> getFeatures() {
        return features;
    }

    FeatureToggle getToggle(final String name) {
        return cache.get(name);
    }
}
