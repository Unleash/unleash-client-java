package io.getunleash.metric;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import io.getunleash.lang.Nullable;

class MetricsBucket {
    private final ConcurrentMap<String, ToggleCount> toggles;
    private final LocalDateTime start;
    @Nullable private volatile LocalDateTime stop;

    MetricsBucket() {
        this.start = LocalDateTime.now(ZoneId.of("UTC"));
        this.toggles = new ConcurrentHashMap<>();
    }

    void registerCount(String toggleName, boolean active) {
        getOrCreate(toggleName).register(active);
    }

    void registerCount(String toggleName, String variantName) {
        getOrCreate(toggleName).register(variantName);
    }

    private ToggleCount getOrCreate(String toggleName) {
        return toggles.computeIfAbsent(toggleName, s -> new ToggleCount());
    }

    void end() {
        this.stop = LocalDateTime.now(ZoneId.of("UTC"));
    }

    public Map<String, ToggleCount> getToggles() {
        return toggles;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public @Nullable LocalDateTime getStop() {
        return stop;
    }
}
