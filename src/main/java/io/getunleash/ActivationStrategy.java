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
    private final List<Integer> segments;

    public ActivationStrategy(String name, @Nullable Map<String, String> parameters) {
        this(name, parameters, Collections.emptyList(), Collections.emptyList());
    }

    public ActivationStrategy(
            String name,
            @Nullable Map<String, String> parameters,
            List<Constraint> constraints,
            List<Integer> segments) {
        this.name = name;
        this.parameters = parameters;
        this.constraints = constraints;
        this.segments = segments;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<Constraint> getConstraints() {
        return Stream.of(
                        constraints,
                        segments.stream()
                                .map(
                                        (segmentId) ->
                                                FeatureRepository.getInstance()
                                                        .getSegment(segmentId))
                                .filter(Objects::nonNull)
                                .map(Segment::getConstraints)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
