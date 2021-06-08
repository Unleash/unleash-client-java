package io.getunleash.repository;

import io.getunleash.UnleashException;

public interface ToggleFetcher {
    FeatureToggleResponse fetchToggles() throws UnleashException;
}
