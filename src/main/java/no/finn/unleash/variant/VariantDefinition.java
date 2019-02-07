package no.finn.unleash.variant;

import java.util.Collections;
import java.util.List;

import no.finn.unleash.Variant;

public class VariantDefinition {

    private final String name;
    private final int weight;
    private final String payload;
    private final List<VariantOverride> overrides;

    public VariantDefinition(String name, int weight, String payload, List<VariantOverride> overrides) {
        this.name = name;
        this.weight = weight;
        this.payload = payload;
        this.overrides = overrides;
    }

    public VariantDefinition(String name, int weight) {
        this(name, weight, null, Collections.emptyList());
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public String getPayload() {
        return payload;
    }

    public List<VariantOverride> getOverrides() {
        return overrides;
    }

    public Variant toVariant() {
        return new Variant(name, payload, true);
    }
}
