package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;

import java.util.Collections;
import java.util.List;

public final class FeatureToggleResponse {
    public enum Status {NOT_CHANGED, CHANGED, UNAVAILABLE}

    private final Status status;
    private final ToggleCollection toggleCollection;

    public FeatureToggleResponse(Status status, ToggleCollection toggleCollection) {
        this.status = status;
        this.toggleCollection = toggleCollection;
    }

    public FeatureToggleResponse(Status status) {
        this.status = status;
        List<FeatureToggle> emptyList = Collections.emptyList();
        this.toggleCollection = new ToggleCollection(emptyList);
    }

    public Status getStatus() {
        return status;
    }

    public ToggleCollection getToggleCollection() {
        return toggleCollection;
    }
}
