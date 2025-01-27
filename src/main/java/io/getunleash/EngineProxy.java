package io.getunleash;

import io.getunleash.metric.UnleashMetricService;
import io.getunleash.repository.FeatureRepository;

public interface EngineProxy extends FeatureRepository, UnleashMetricService {}
