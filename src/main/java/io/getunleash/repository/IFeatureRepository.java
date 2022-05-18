package io.getunleash.repository;

import io.getunleash.Segment;
import io.getunleash.lang.Nullable;

public interface IFeatureRepository extends ToggleRepository {
    @Nullable
    Segment getSegment(Integer id);

}
