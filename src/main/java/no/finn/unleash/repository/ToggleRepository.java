package no.finn.unleash.repository;

import java.util.List;
import no.finn.unleash.FeatureToggle;

public interface ToggleRepository {
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
