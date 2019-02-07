package no.finn.unleash.integration;

import no.finn.unleash.Variant;

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
        if(expectedResult.getName().equals("disabled")) {
            return Variant.DISABLED_VARIANT;
        }

        return expectedResult;
    }
}
