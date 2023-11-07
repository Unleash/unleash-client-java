package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.UnleashException;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.lang.Nullable;
import java.util.Collections;
import java.util.List;

public class FeatureToggleResponse implements UnleashEvent {

    public enum Status {
        NOT_CHANGED,
        CHANGED,
        UNAVAILABLE,
    }

    private final Status status;
    private final int httpStatusCode;
    private final ToggleCollection toggleCollection;
    @Nullable private String location;

    public FeatureToggleResponse(Status status, ToggleCollection toggleCollection) {
        this.status = status;
        this.httpStatusCode = 200;
        this.toggleCollection = toggleCollection;
    }

    public FeatureToggleResponse(Status status, int httpStatusCode) {
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        List<FeatureToggle> emptyList = Collections.emptyList();
        this.toggleCollection = new ToggleCollection(emptyList);
    }

    public FeatureToggleResponse(Status status, int httpStatusCode, @Nullable String location) {
        this(status, httpStatusCode);
        this.location = location;
    }

    public Status getStatus() {
        return status;
    }

    public ToggleCollection getToggleCollection() {
        return toggleCollection;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public @Nullable String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "FeatureToggleResponse:"
                + " status="
                + status
                + " httpStatus="
                + httpStatusCode
                + " location="
                + location;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        if (status == FeatureToggleResponse.Status.UNAVAILABLE) {
            String msg =
                    "Error fetching toggles from Unleash API - StatusCode: " + getHttpStatusCode();
            if (location != null) {
                msg += ", Location: " + location;
            }
            unleashSubscriber.onError(new UnleashException(msg, null));
        }

        unleashSubscriber.togglesFetched(this);
    }
}
