package io.getunleash;

import io.getunleash.lang.Nullable;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class FeatureDependency {
    public String feature;
    @Nullable public Boolean enabled;
    @Nullable public List<String> variants;

    public FeatureDependency(String feature) {
        this.feature = feature;
    }

    public FeatureDependency(
            String feature, @Nullable Boolean enabled, @Nullable List<String> variants) {
        this.feature = feature;
        this.enabled = enabled;
        this.variants = variants;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public boolean isEnabled() {
        return enabled == null || enabled; // Default value here should be true
    }

    public void setEnabled(@Nullable Boolean enabled) {
        this.enabled = enabled;
    }

    @Nonnull
    public List<String> getVariants() {
        if (variants != null) {
            return variants;
        }
        return Collections.emptyList();
    }

    public void setVariants(@Nullable List<String> variants) {
        this.variants = variants;
    }
}
