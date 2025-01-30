package io.getunleash.repository;

import io.getunleash.FeatureDefinition;
import io.getunleash.UnleashContext;
import io.getunleash.engine.VariantDef;
import java.util.Optional;
import java.util.stream.Stream;

public interface FeatureRepository {

    Boolean isEnabled(String toggleName, UnleashContext context);

    Optional<VariantDef> getVariant(String toggleName, UnleashContext context);

    Stream<FeatureDefinition> listKnownToggles();

    boolean shouldEmitImpressionEvent(String toggleName);
}
