package io.getunleash;

import io.getunleash.lang.Nullable;
import io.getunleash.repository.FeatureRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ActivationStrategy {
    private final String name;
    private final @Nullable Map<String, String> parameters;
    private final List<Constraint> constraints;
    private final List<Integer> segmentIds;
    private final @Nullable FeatureRepository repository;

    public ActivationStrategy(String name, @Nullable Map<String, String> parameters) {
        this(name, parameters, Collections.emptyList(), Collections.emptyList(), null);
    }

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
            List<Integer> segmentIds,
            @Nullable FeatureRepository repository) {
        this.name = name;
        this.parameters = parameters;
        this.constraints = constraints;
        this.segmentIds = segmentIds;
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<Constraint> getConstraints() {
        if (repository != null) {
            return Stream.of(
                            constraints,
                            segmentIds.stream()
                                    .map(repository::getSegment)
                                    .filter(Objects::nonNull)
                                    .map(Segment::getConstraints)
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList()))
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }

        return constraints;
    }
}
