package io.getunleash.repository;

import io.getunleash.FeatureDefinition;
import io.getunleash.Segment;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.getunleash.lang.Nullable;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface FeatureRepository {
    @Nullable
    Segment getSegment(Integer id);

    void addConsumer(Consumer<FeatureCollection> consumer);

    Boolean isEnabled(String toggleName, UnleashContext context);

    Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue);

    Stream<FeatureDefinition> listKnownToggles();

    boolean shouldEmitImpressionEvent(String toggleName);
}
