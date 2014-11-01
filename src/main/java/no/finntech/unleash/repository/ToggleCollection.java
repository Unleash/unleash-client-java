package no.finntech.unleash.repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import no.finntech.unleash.Toggle;

final class ToggleCollection {
    private Collection<Toggle> features = Collections.emptyList();
    private transient Map<String, Toggle> cache;

    ToggleCollection(final Collection<Toggle> features) {
        this.features = features;
        cache = new HashMap<>();
        for(Toggle toggle : features) {
            cache.put(toggle.getName(), toggle);
        }
    }

    Collection<Toggle> getFeatures() {
        return features;
    }

    Toggle getToggle(final String name) {
        return cache.get(name);
    }
}