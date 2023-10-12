package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.variant.VariantDefinition;
import java.util.*;
import javax.annotation.Nonnull;

public final class ActivationStrategy {
    private final String name;
    private final Map<String, String> parameters;
    private final List<Constraint> constraints;
    private final List<Integer> segments;
    private final List<VariantDefinition> variants;

    public ActivationStrategy(String name, @Nullable Map<String, String> parameters) {
        this(
                name,
                parameters,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
    }

    public ActivationStrategy(
            String name,
            @Nullable Map<String, String> parameters,
            List<Constraint> constraints,
            List<Integer> segments,
            List<VariantDefinition> variants) {
        this.name = name;
        this.parameters = Optional.ofNullable(parameters).orElseGet(Collections::emptyMap);
        this.constraints = Optional.ofNullable(constraints).orElseGet(Collections::emptyList);
        this.segments = Optional.ofNullable(segments).orElseGet(Collections::emptyList);
        this.variants = Optional.ofNullable(variants).orElseGet(Collections::emptyList);
    }

    public String getName() {
        return name;
    }

    public List<Integer> getSegments() {
        return segments;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    @Nonnull
    public List<VariantDefinition> getVariants() {

        return variants;
    }
}
