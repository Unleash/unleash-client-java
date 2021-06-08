package io.getunleash.integration;

public class TestCase {
    private String description;
    private UnleashContextDefinition context;
    private String toggleName;
    private boolean expectedResult;

    public UnleashContextDefinition getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }

    public String getToggleName() {
        return toggleName;
    }

    public boolean getExpectedResult() {
        return expectedResult;
    }
}
