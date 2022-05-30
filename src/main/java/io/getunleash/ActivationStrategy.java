package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.repository.FeatureRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ActivationStrategy {
    private final String name;
    private final Map<String, String> parameters;
    private final List<Constraint> constraints;
    private final List<Integer> segments;
    @Nullable private final FeatureRepository repository;

    public ActivationStrategy(
            String name,
            @Nullable Map<String, String> parameters,
            @Nullable FeatureRepository repository) {
        this(name, parameters, Collections.emptyList(), Collections.emptyList(), repository);
    }

    public ActivationStrategy(
            String name,
            @Nullable Map<String, String> parameters,
            List<Constraint> constraints,
            List<Integer> segments,
            @Nullable FeatureRepository repository) {
        this.name = name;
        this.parameters = Optional.ofNullable(parameters).orElseGet(Collections::emptyMap);
        this.constraints = Optional.ofNullable(constraints).orElseGet(Collections::emptyList);
        this.segments = Optional.ofNullable(segments).orElseGet(Collections::emptyList);
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<Constraint> getConstraints() {
        if (repository == null) {
            return constraints;
        }
        return Stream.of(
                        Optional.ofNullable(constraints).orElseGet(Collections::emptyList),
                        Optional.ofNullable(segments).orElseGet(Collections::emptyList).stream()
                                .map(this.repository::getSegment)
                                .filter(Objects::nonNull)
                                .map(Segment::getConstraints)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
