package no.finn.unleash.metric;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

class MetricsBucket {
    private final Map<String, ToggleCount> toggles;
    private final LocalDateTime start;
    private LocalDateTime stop;

    MetricsBucket() {
        this.start = LocalDateTime.now(ZoneId.of("UTC"));
        this.toggles = new HashMap<>();
    }

    void registerCount(String toggleName, boolean active) {
        if(toggles.containsKey(toggleName)) {
            toggles.get(toggleName).register(active);
        } else {
            ToggleCount counter = new ToggleCount();
            counter.register(active);
            toggles.put(toggleName, counter);
        }
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

    public LocalDateTime getStop() {
        return stop;
    }
}
