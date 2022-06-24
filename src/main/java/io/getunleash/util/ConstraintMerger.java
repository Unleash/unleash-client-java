package io.getunleash.util;

import io.getunleash.ActivationStrategy;
import io.getunleash.Constraint;
import io.getunleash.Segment;
import io.getunleash.repository.FeatureRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstraintMerger {
    public static List<Constraint> mergeConstraints(
            FeatureRepository repository, ActivationStrategy strategy) {
        return Stream.of(
                        Optional.ofNullable(strategy.getConstraints())
                                .orElseGet(Collections::emptyList),
                        Optional.ofNullable(strategy.getSegments())
                                .orElseGet(Collections::emptyList).stream()
                                .map(repository::getSegment)
                                .filter(Objects::nonNull)
                                .map(Segment::getConstraints)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
