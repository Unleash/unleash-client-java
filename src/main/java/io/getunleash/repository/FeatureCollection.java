package io.getunleash.repository;

public final class FeatureCollection {

    private final ToggleCollection toggleCollection;
    private final SegmentCollection segmentCollection;

    public FeatureCollection(ToggleCollection toggleCollection, SegmentCollection segmentCollection) {
        this.toggleCollection = toggleCollection;
        this.segmentCollection = segmentCollection;
    }

    public ToggleCollection getToggleCollection() {
        return toggleCollection;
    }

    public SegmentCollection getSegmentCollection() {
        return segmentCollection;
    }
}
