package no.finn.unleash.strategy;

public class Variant {

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

    public String getPayload() {
        return payload;
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
