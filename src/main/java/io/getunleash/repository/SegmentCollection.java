package io.getunleash.repository;

import io.getunleash.Segment;
import io.getunleash.lang.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SegmentCollection implements Serializable {

    static final long serialVersionUID = 1214L;
    private final Collection<Segment> segments;
    private final transient Map<Integer, Segment> cache;

    public SegmentCollection(final Collection<Segment> segments) {
        this.segments = ensureNotNull(segments);
        if (this.segments.size() > 0) {
            this.cache =
                    segments.stream()
                            .collect(
                                    Collectors.toConcurrentMap(
                                            Segment::getId, Function.identity()));
        } else {
            this.cache = new ConcurrentHashMap<>();
        }
    }

    private Collection<Segment> ensureNotNull(@Nullable Collection<Segment> segments) {
        return Optional.ofNullable(segments).orElseGet(Collections::emptyList);
    }

    public Collection<Segment> getSegments() {
        return Collections.unmodifiableCollection(segments);
    }

    public Segment getSegment(final Integer id) {
        return cache.get(id);
    }
}
