package io.getunleash.repository;

import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import java.util.Optional;

public final class ClientFeaturesResponse implements UnleashEvent {
    public enum Status {
        NOT_CHANGED,
        CHANGED,
        UNAVAILABLE,
    }

    private final Optional<String> clientFeatures;
    private final Status statusCode;
    private final int httpStatusCode;

    private ClientFeaturesResponse(Status status, int httpStatusCode, Optional<String> clientFeatures) {
        this.statusCode = status;
        this.clientFeatures = clientFeatures;
        this.httpStatusCode = httpStatusCode;
    }

    public static ClientFeaturesResponse notChanged() {
        return new ClientFeaturesResponse(Status.NOT_CHANGED, 304, Optional.empty());
    }

    public static ClientFeaturesResponse updated(String clientFeatures) {
        return new ClientFeaturesResponse(Status.CHANGED, 200, Optional.of(clientFeatures));
    }

    public static ClientFeaturesResponse unavailable(int statusCode) {
        return new ClientFeaturesResponse(Status.UNAVAILABLE, statusCode, Optional.empty());
    }

    public Optional<String> getClientFeatures() {
        return clientFeatures;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public Status getStatus() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "ClientFeatureResponse:"
                + " status="
                + this.getStatus()
                + " httpStatus="
                + this.getHttpStatusCode();
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'publishTo'");
    }
}
