package no.finn.unleash.variant;

import java.util.Objects;

public class Payload {
    private String type;
    private String value;

    public Payload(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payload payload = (Payload) o;
        return Objects.equals(type, payload.type) && Objects.equals(value, payload.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }
}
