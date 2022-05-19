package io.getunleash.repository;

import io.getunleash.lang.Nullable;
import java.io.Serializable;
import java.util.Collections;

public final class ClientFeaturesResponse extends FeatureToggleResponse implements Serializable {
    private final long serialVersionUID = 1L;
    private final int version;
    private final @Nullable SegmentCollection segmentCollection;

    public ClientFeaturesResponse(Status status, int httpStatusCode) {
        super(status, httpStatusCode);
        this.version = 1;
        this.segmentCollection = new SegmentCollection(Collections.emptyList());
    }

    public ClientFeaturesResponse(
            Status status,
            int httpStatusCode,
            @Nullable SegmentCollection segmentCollection,
            @Nullable int version) {
        super(status, httpStatusCode);
        this.version = version;
        this.segmentCollection = segmentCollection;
    }

    public ClientFeaturesResponse(
            Status status,
            ToggleCollection toggleCollection,
            @Nullable SegmentCollection segmentCollection) {
        super(status, toggleCollection);
        this.version = 1;
        this.segmentCollection = segmentCollection;
    }

    public ClientFeaturesResponse(Status status, FeatureCollection featureCollection) {
        super(status, featureCollection.getToggleCollection());
        this.version = 1;
        this.segmentCollection = featureCollection.getSegmentCollection();
    }

    public ClientFeaturesResponse(Status status, int httpStatusCode, @Nullable String location) {
        super(status, httpStatusCode, location);
        this.version = 1;
        this.segmentCollection = new SegmentCollection(Collections.emptyList());
    }

    public int getVersion() {
        return version;
    }

    @Nullable
    public SegmentCollection getSegmentCollection() {
        return segmentCollection;
    }

    @Override
    public String toString() {
        return "ClientFeatureResponse:"
                + " status="
                + this.getStatus()
                + " httpStatus="
                + this.getHttpStatusCode();
    }
}
