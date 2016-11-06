package no.finn.unleash.repository;

import no.finn.unleash.UnleashException;

public interface ToggleFetcher {
   FeatureToggleResponse fetchToggles() throws UnleashException;
}
