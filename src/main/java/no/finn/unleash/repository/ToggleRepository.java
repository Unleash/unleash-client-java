package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;

import java.util.List;
import java.util.Observer;

public interface ToggleRepository {
    FeatureToggle getToggle(String name);

    List<String> getFeatureNames();

    void addObserver(Observer o);

    void deleteObserver(Observer o);
}
