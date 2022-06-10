package io.getunleash.repository;

import io.getunleash.UnleashException;

public interface FeatureFetcher {
    ClientFeaturesResponse fetchFeatures() throws UnleashException;
}
