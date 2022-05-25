package io.getunleash.repository;

import io.getunleash.UnleashException;
@Deprecated()
public interface ToggleFetcher {
    FeatureToggleResponse fetchToggles() throws UnleashException;
}
