package io.getunleash.repository;

import io.getunleash.FeatureDefinition;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import java.util.stream.Stream;

public interface FeatureRepository {

    Boolean isEnabled(String toggleName, UnleashContext context);

    Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue);

    Stream<FeatureDefinition> listKnownToggles();

    boolean shouldEmitImpressionEvent(String toggleName);
}
