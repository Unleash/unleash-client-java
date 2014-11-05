package no.finn.unleash;

import java.util.HashSet;
import java.util.Set;

public final class FakeUnleash implements Unleash {
    private boolean enableAll = false;
    private Set<String> enabledFeatures = new HashSet<>();

    @Override
    public boolean isEnabled(String toggleName) {
        return isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(String toggleName, boolean defaultSetting) {
        if(enableAll) {
            return true;
        } else if(enabledFeatures.contains(toggleName)) {
            return true;
        } else {
            return defaultSetting;
        }
    }

    public void enableAll() {
        enableAll = true;
    }

    public void disableAll() {
        enabledFeatures = new HashSet<>();
        enableAll = false;
    }

    public void enable(String... features) {
        for(String name: features) {
            enabledFeatures.add(name);
        }
    }

    public void disable(String... features) {
        for(String name: features) {
            enabledFeatures.remove(name);
        }
    }
}
