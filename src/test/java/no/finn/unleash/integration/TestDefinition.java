package no.finn.unleash.integration;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

public class TestDefinition {
    private String name;
    private JsonObject state;

    public String getName() {
        return name;
    }

    public JsonObject getState() {
        return state;
    }

    public List<TestCase> getTests() {
        return tests;
    }

    public List<TestCase> tests;
}
