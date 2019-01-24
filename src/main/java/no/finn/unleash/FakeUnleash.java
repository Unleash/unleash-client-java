package no.finn.unleash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import no.finn.unleash.strategy.Variant;

public final class FakeUnleash implements Unleash {
    private boolean enableAll = false;
    private boolean disableAll = false;
    private Map<String, Boolean> features = new HashMap<>();

    @Override
    public boolean isEnabled(String toggleName) {
        return isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        if(enableAll) {
            return true;
        } else if(disableAll) {
            return false;
        } else {
            return features.getOrDefault(toggleName, defaultSetting);
        }
    }

    @Override
    public Variant getVariant(final String toggleName, final UnleashContext context) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public Variant getVariant(final String toggleName, final UnleashContext context, final String defaultPayload) {
        throw new IllegalStateException("Not implemented");
    }

    @Override
    public List<String> getFeatureToggleNames() {
        return features.keySet().stream().collect(Collectors.toList());
    }

    public void enableAll() {
        disableAll = false;
        enableAll = true;
        features.clear();
    }

    public void disableAll() {
        disableAll = true;
        enableAll = false;
        features.clear();
    }

    public void resetAll() {
        disableAll = false;
        enableAll = false;
        features.clear();
    }

    public void enable(String... features) {
        for(String name: features) {
            this.features.put(name, true);
        }
    }

    public void disable(String... features) {
        for(String name: features) {
            this.features.put(name, false);
        }
    }

    public void reset(String... features) {
        for(String name: features) {
            this.features.remove(name);
        }
    }
}
