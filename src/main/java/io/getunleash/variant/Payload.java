package io.getunleash.variant;

import java.util.Objects;
import io.getunleash.lang.Nullable;

public class Payload {
    private String type;
    @Nullable private String value;

    public Payload(String type, @Nullable String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public @Nullable String getValue() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payload payload = (Payload) o;
        return Objects.equals(type, payload.type) && Objects.equals(value, payload.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return "Payload{" + "type='" + type + '\'' + ", value='" + value + '\'' + '}';
    }
}
