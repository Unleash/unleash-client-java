package no.finn.unleash;

import java.util.Objects;
import java.util.Optional;
import no.finn.unleash.variant.Payload;

public class Variant {
    public static final Variant DISABLED_VARIANT = new Variant("disabled", (String) null, false);

    private final String name;
    private final Payload payload;
    private final boolean enabled;

    public Variant(String name, Payload payload, boolean enabled) {
        this.name = name;
        this.payload = payload;
        this.enabled = enabled;
    }

    public Variant(String name, String payload, boolean enabled) {
        this.name = name;
        this.payload = new Payload("string", payload);
        this.enabled = enabled;
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
