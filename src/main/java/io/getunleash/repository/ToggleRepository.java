package io.getunleash.repository;

import java.util.List;
import io.getunleash.FeatureToggle;
import io.getunleash.lang.Nullable;

public interface ToggleRepository {
    @Nullable
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
