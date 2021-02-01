package no.finn.unleash.variant;

import java.util.List;

public class VariantOverride {
    private String contextName;
    private List<String> values;

    public VariantOverride(String contextName, List<String> values) {
        this.contextName = contextName;
        this.values = values;
    }

    public String getContextName() {
        return contextName;
    }

    public List<String> getValues() {
        return values;
    }
}
