package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.lang.Nullable;
import java.util.List;

public interface ToggleRepository {
    @Nullable
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
