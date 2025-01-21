package io.getunleash;

import io.getunleash.engine.FeatureDef;
import java.util.Optional;

public class FeatureDefinition {

    private final String name;
    private final Optional<String> type;
    private final String project;

    public FeatureDefinition(FeatureDef source) {
        this.name = source.getName();
        this.type = source.getType();
        this.project = source.getProject();
    }

    FeatureDefinition(String name, Optional<String> type, String project) {
        this.name = name;
        this.type = type;
        this.project = project;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getType() {
        return type;
    }

    public String getProject() {
        return project;
    }
}
