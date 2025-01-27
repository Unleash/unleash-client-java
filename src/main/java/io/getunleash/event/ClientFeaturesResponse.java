package io.getunleash.event;

import io.getunleash.FeatureDefinition;
import io.getunleash.util.ClientFeaturesParser;
import java.util.List;
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
    private final Optional<String> location;
    private List<FeatureDefinition> features;

    private ClientFeaturesResponse(
            Status status,
            int httpStatusCode,
            Optional<String> clientFeatures,
            Optional<String> location) {
        this.statusCode = status;
        this.clientFeatures = clientFeatures;
        this.httpStatusCode = httpStatusCode;
        this.location = location;
    }

    public static ClientFeaturesResponse notChanged() {
        return new ClientFeaturesResponse(
                Status.NOT_CHANGED, 304, Optional.empty(), Optional.empty());
    }

    public static ClientFeaturesResponse updated(String clientFeatures) {
        return new ClientFeaturesResponse(
                Status.CHANGED, 200, Optional.of(clientFeatures), Optional.empty());
    }

    public static ClientFeaturesResponse unavailable(int statusCode, Optional<String> location) {
        return new ClientFeaturesResponse(
                Status.UNAVAILABLE, statusCode, Optional.empty(), location);
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

    public List<FeatureDefinition> getFeatures() {
        if (clientFeatures.isPresent() && features == null) {
            features = ClientFeaturesParser.parse(clientFeatures.get());
        }
        return features;
    }

    public String getLocation() {
        return location.orElse(null);
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
        unleashSubscriber.togglesFetched(this);
    }
}
