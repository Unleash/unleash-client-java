package no.finn.unleash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FakeUnleash implements Unleash {
    private boolean enableAll = false;
    private boolean disableAll = false;
    private Map<String, Boolean> features = new HashMap<>();
    private Map<String, Variant> variants = new HashMap<>();

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
    public boolean isEnabled(String toggleName, boolean defaultSetting, FallbackAction fallbackAction) {
        if(enableAll) {
            return true;
        } else if(disableAll) {

            return false;
        } else {
            if(!features.containsKey(toggleName)) {
                fallbackAction.apply(toggleName, UnleashContext.builder().build());
            }
            return features.getOrDefault(toggleName, defaultSetting);
        }
    }

    @Override
    public boolean isEnabled(String toggleName, FallbackAction fallbackAction) {
        if(enableAll) {
            return true;
        } else if(disableAll) {
            return false;
        } else {
            if(!features.containsKey(toggleName)) {
                fallbackAction.apply(toggleName, UnleashContext.builder().build());
            }
            return features.getOrDefault(toggleName, false);
        }
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, Variant.DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        return getVariant(toggleName, defaultValue);
    }

    @Override
    public Variant getVariant(String toggleName) {
        return getVariant(toggleName, Variant.DISABLED_VARIANT);
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        if(isEnabled(toggleName) && variants.containsKey(toggleName)) {
            return variants.get(toggleName);
        } else {
            return defaultValue;
        }
    }

    @Override
    public List<String> getFeatureToggleNames() {
        return new ArrayList<>(features.keySet());
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

    public void setVariant(String t1, Variant a) {
        variants.put(t1, a);
    }
}
