package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import java.util.Collections;

public final class FeatureCollection {

    private final ToggleCollection toggleCollection;
    private final SegmentCollection segmentCollection;

    public FeatureCollection() {
        this(
                new ToggleCollection(Collections.emptyList()),
                new SegmentCollection(Collections.emptyList()));
    }

    public FeatureCollection(
            ToggleCollection toggleCollection, SegmentCollection segmentCollection) {
        this.toggleCollection = toggleCollection;
        this.segmentCollection = segmentCollection;
    }

    public ToggleCollection getToggleCollection() {
        return toggleCollection;
    }

    public SegmentCollection getSegmentCollection() {
        return segmentCollection;
    }

    public FeatureToggle getToggle(String name) {
        return toggleCollection.getToggle(name);
    }

    public Segment getSegment(int id) {
        return segmentCollection.getSegment(id);
    }
}
