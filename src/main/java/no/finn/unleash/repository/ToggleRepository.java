package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;

import java.util.List;

public interface ToggleRepository {
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();
}
