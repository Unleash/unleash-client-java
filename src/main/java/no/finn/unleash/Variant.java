package no.finn.unleash;

import java.util.Optional;

public class Variant {
    public static final Variant DISABLED_VARIANT = new Variant("disabled", null, false);

    private final String name;
    private final String payload;
    private final boolean enabled;

    public Variant(String name, String payload, boolean enabled) {
        this.name = name;
        this.payload = payload;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getPayload() {
        return Optional.ofNullable(payload);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "Variant{" +
            "name='" + name + '\'' +
            ", payload='" + payload + '\'' +
            ", enabled=" + enabled +
            '}';
    }
}
