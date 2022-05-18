package io.getunleash;

import io.getunleash.repository.FeatureRepository;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ActivationStrategy {
    private final String name;
    private final Map<String, String> parameters;
    private final List<Constraint> constraints;
    private final List<Integer> segmentIds;
    private final FeatureRepository repository;

    public ActivationStrategy(String name, Map<String, String> parameters, FeatureRepository repository) {
        this(name, parameters, Collections.emptyList(), Collections.emptyList(), repository);
    }

    public ActivationStrategy(
            String name, Map<String, String> parameters, List<Constraint> constraints, List<Integer> segmentIds, FeatureRepository repository) {
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
        return Stream.of(constraints,
                         segmentIds.stream()
                            .map(repository::getSegment)
                            .filter(Objects::nonNull)
                            .map(Segment::getConstraints)
                            .flatMap(Collection::stream).collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

}
