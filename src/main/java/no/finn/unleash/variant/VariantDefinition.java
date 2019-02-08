package no.finn.unleash.variant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.finn.unleash.Variant;

public class VariantDefinition {

    private final String name;
    private final int weight;
    private final Payload payload;
    private final List<VariantOverride> overrides;

    public VariantDefinition(String name, int weight, Payload payload, List<VariantOverride> overrides) {
        this.name = name;
        this.weight = weight;
        this.payload = payload;
        this.overrides = overrides;
    }

    VariantDefinition(String name, int weight) {
        this(name, weight, null, Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public Payload getPayload() {
        return payload;
    }

    List<VariantOverride> getOverrides() {
        if(overrides == null) {
            return Collections.emptyList();
        } else {
            return overrides;
        }
    }

    Variant toVariant() {
        return new Variant(name, payload, true);
    }
}
