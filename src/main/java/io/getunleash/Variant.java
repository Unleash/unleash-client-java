package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.Payload;
import java.util.Objects;
import java.util.Optional;

public class Variant {

    public static Variant disabledVariant(boolean featureEnabled) {
        return new Variant("disabled", (String) null, false, featureEnabled);
    }

    private final String name;
    @Nullable private final Payload payload;
    private final boolean enabled;
    @Nullable private final String stickiness;

    private final boolean featureEnabled;


    public Variant(String name, @Nullable Payload payload, boolean enabled, boolean featureEnabled) {
        this(name, payload, enabled, null, featureEnabled);
    }

    public Variant(String name, @Nullable Payload payload, boolean enabled, String stickiness, boolean featureEnabled) {
        this.name = name;
        this.payload = payload;
        this.enabled = enabled;
        this.stickiness = stickiness;
        this.featureEnabled = featureEnabled;
    }

    public Variant(String name, @Nullable String payload, boolean enabled, boolean featureEnabled) {
        this(name, payload, enabled, null, featureEnabled);
    }

    public Variant(String name, @Nullable String payload, boolean enabled, String stickiness, boolean featureEnabled) {
        this.name = name;
        this.payload = new Payload("string", payload);
        this.enabled = enabled;
        this.stickiness = stickiness;
        this.featureEnabled = featureEnabled;
    }

    public String getName() {
        return name;
    }

    public Optional<Payload> getPayload() {
        return Optional.ofNullable(payload);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isFeatureEnabled() {
        return featureEnabled;
    }

    @Nullable
    public String getStickiness() {
        return stickiness;
    }

    @Override
    public String toString() {
        return "Variant{"
                + "name='"
                + name
                + '\''
                + ", payload='"
                + payload
                + '\''
                + ", enabled="
                + enabled
                + ", featureEnabled="
                + featureEnabled
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variant variant = (Variant) o;
        return enabled == variant.enabled
                && featureEnabled == variant.featureEnabled
                && Objects.equals(name, variant.name)
                && Objects.equals(payload, variant.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, payload, enabled);
    }
}
