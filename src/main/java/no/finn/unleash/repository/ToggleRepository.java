package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;

public interface ToggleRepository {
    FeatureToggle getToggle(String name);
}
