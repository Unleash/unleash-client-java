package io.getunleash.util;

import io.getunleash.ActivationStrategy;
import io.getunleash.Constraint;
import io.getunleash.Segment;
import io.getunleash.repository.IFeatureRepository;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.getunleash.Segment.DENY_SEGMENT;

public class ConstraintMerger {
    public static List<Constraint> mergeConstraints(
            IFeatureRepository repository, ActivationStrategy strategy) {
        return Stream.of(
                        Optional.ofNullable(strategy.getConstraints())
                                .orElseGet(Collections::emptyList),
                        Optional.ofNullable(strategy.getSegments())
                                .orElseGet(Collections::emptyList)
                                .stream()
                                .map(repository::getSegment)
                                .map(s -> s == null? DENY_SEGMENT : s)
                                .map(Segment::getConstraints)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
