package io.getunleash.integration;

import io.getunleash.variant.Variant;

public class TestCaseVariant {
    private String description;
    private UnleashContextDefinition context;
    private String toggleName;
    private Variant expectedResult;

    public UnleashContextDefinition getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }

    public String getToggleName() {
        return toggleName;
    }

    public Variant getExpectedResult() {
        if (expectedResult.getName().equals("disabled")) {
            Variant clone =
                    new Variant(
                            Variant.DISABLED_VARIANT.getName(),
                            Variant.DISABLED_VARIANT.getPayload().orElse(null),
                            Variant.DISABLED_VARIANT.isEnabled(),
                            Variant.DISABLED_VARIANT.getStickiness(),
                            expectedResult.isFeatureEnabled());
            return clone;
        }

        return expectedResult;
    }
}
