package no.finn.unleash;

import java.util.Objects;
import java.util.Optional;
import no.finn.unleash.lang.Nullable;
import no.finn.unleash.variant.Payload;

public class Variant {
    public static final Variant DISABLED_VARIANT = new Variant("disabled", (String) null, false);

    private final String name;
    @Nullable private final Payload payload;
    private final boolean enabled;
    @Nullable private final String stickiness;

    public Variant(String name, @Nullable Payload payload, boolean enabled) {
        this(name, payload, enabled, null);
    }

    public Variant(String name, @Nullable Payload payload, boolean enabled, String stickiness) {
        this.name = name;
        this.payload = payload;
        this.enabled = enabled;
        this.stickiness = stickiness;
    }

    public Variant(String name, @Nullable String payload, boolean enabled) {
        this(name, payload, enabled, null);
    }

    public Variant(String name, @Nullable String payload, boolean enabled, String stickiness) {
        this.name = name;
        this.payload = new Payload("string", payload);
        this.enabled = enabled;
        this.stickiness = stickiness;
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
