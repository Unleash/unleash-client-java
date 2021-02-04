package no.finn.unleash.repository;

import java.util.List;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.lang.Nullable;

public interface ToggleRepository {
    @Nullable FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
