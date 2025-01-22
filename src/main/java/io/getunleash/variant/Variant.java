package io.getunleash.variant;

import io.getunleash.lang.Nullable;
import java.util.Objects;
import java.util.Optional;

public class Variant {
    public static final Variant DISABLED_VARIANT = new Variant("disabled", (String) null, false);

    private final String name;
    @Nullable private final Payload payload;
    private final boolean enabled;
    @Nullable private final String stickiness;
    private final boolean feature_enabled;

    public Variant(
            String name, @Nullable Payload payload, boolean enabled, boolean feature_enabled) {
        this(name, payload, enabled, null, feature_enabled);
    }

    public Variant(
            String name,
            @Nullable Payload payload,
            boolean enabled,
            String stickiness,
            boolean feature_enabled) {
        this.name = name;
        this.payload = payload;
        this.enabled = enabled;
        this.stickiness = stickiness;
        this.feature_enabled = feature_enabled;
    }

    public Variant(String name, @Nullable String payload, boolean enabled) {
        this(name, payload, enabled, null, false);
    }

    public Variant(
            String name,
            @Nullable String payload,
            boolean enabled,
            String stickiness,
            boolean feature_enabled) {
        this.name = name;
        this.payload = new Payload("string", payload);
        this.enabled = enabled;
        this.stickiness = stickiness;
        this.feature_enabled = feature_enabled;
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
        return feature_enabled;
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
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variant variant = (Variant) o;
        return enabled == variant.enabled
                && Objects.equals(name, variant.name)
                && Objects.equals(payload, variant.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, payload, enabled);
    }
}
