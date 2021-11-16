package io.getunleash.util;

import io.getunleash.repository.ToggleFetcher;
import java.util.function.Function;

public interface UnleashToggleFetcherFactory extends Function<UnleashConfig, ToggleFetcher> {}
