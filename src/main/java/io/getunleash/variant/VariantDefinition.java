package io.getunleash.variant;

import io.getunleash.Variant;
import io.getunleash.lang.Nullable;
import java.util.Collections;
import java.util.List;

public class VariantDefinition {

    private final String name;
    private final int weight;
    @Nullable private final Payload payload;
    @Nullable private final List<VariantOverride> overrides;
    @Nullable private final String stickiness;

    public VariantDefinition(
            String name,
            int weight,
            @Nullable Payload payload,
            @Nullable List<VariantOverride> overrides) {
        this(name, weight, payload, overrides, null);
    }

    public VariantDefinition(
            String name,
            int weight,
            @Nullable Payload payload,
            @Nullable List<VariantOverride> overrides,
            @Nullable String stickiness) {
        this.name = name;
        this.weight = weight;
        this.payload = payload;
        this.overrides = overrides;
        this.stickiness = stickiness;
    }

    VariantDefinition(String name, int weight) {
        this(name, weight, null, Collections.emptyList(), null);
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public @Nullable Payload getPayload() {
        return payload;
    }

    List<VariantOverride> getOverrides() {
        if (overrides == null) {
            return Collections.emptyList();
        } else {
            return overrides;
        }
    }

    public @Nullable String getStickiness() {
        return stickiness;
    }

    Variant toVariant() {
        return new Variant(name, payload, true, stickiness, false);
    }
}
