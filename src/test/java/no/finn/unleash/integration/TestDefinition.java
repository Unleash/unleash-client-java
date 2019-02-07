package no.finn.unleash.integration;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

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
