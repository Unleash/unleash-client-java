package no.finn.unleash.metric;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

class ToggleCount {
    private final AtomicLong yes;
    private final AtomicLong no;
    private final ConcurrentMap<String, AtomicLong> variants;

    public ToggleCount() {
        this.yes = new AtomicLong(0);
        this.no = new AtomicLong(0);
        this.variants = new ConcurrentHashMap<>();
    }

    public void register(boolean active) {
        if (active) {
            yes.incrementAndGet();
        } else {
            no.incrementAndGet();
        }
    }

    public void register(String variantName) {
        AtomicLong current = variants.computeIfAbsent(variantName, s -> new AtomicLong());
        current.incrementAndGet();
    }

    public long getYes() {
        return yes.get();
    }

    public long getNo() {
        return no.get();
    }

    public Map<String, ? extends Number> getVariants() {
        return variants;
    }
}
