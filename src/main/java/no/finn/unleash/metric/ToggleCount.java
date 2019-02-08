package no.finn.unleash.metric;

import java.util.HashMap;
import java.util.Map;

class ToggleCount {
    private long yes;
    private long no;
    private Map<String, Long> variants;

    public ToggleCount() {
        this.yes = 0;
        this.no = 0;
        this.variants = new HashMap<>();
    }

    public void register(boolean active) {
        if(active) {
            yes++;
        } else {
            no++;
        }
    }

    public void register(String variantName) {
        Long current = variants.getOrDefault(variantName, 0L);
        variants.put(variantName, ++current);
    }

    public long getYes() {
        return yes;
    }

    public long getNo() {
        return no;
    }

    public Map<String, Long> getVariants() {
        return variants;
    }
}
