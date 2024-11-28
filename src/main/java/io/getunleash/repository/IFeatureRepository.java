package io.getunleash.repository;

import io.getunleash.Segment;
import io.getunleash.lang.Nullable;
import java.util.function.Consumer;

public interface IFeatureRepository extends ToggleRepository {
    @Nullable
    Segment getSegment(Integer id);

    void addConsumer(Consumer<FeatureCollection> consumer);
}
