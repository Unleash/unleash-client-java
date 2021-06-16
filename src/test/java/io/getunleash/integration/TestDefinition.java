package io.getunleash.integration;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class TestDefinition {
    private String name;
    private JsonObject state;
    private List<TestCase> tests = new ArrayList<>();
    private List<TestCaseVariant> variantTests = new ArrayList<>();

    public String getName() {
        return name;
    }

    public JsonObject getState() {
        return state;
    }

    public List<TestCase> getTests() {
        return tests;
    }

    public List<TestCaseVariant> getVariantTests() {
        return variantTests;
    }
}
