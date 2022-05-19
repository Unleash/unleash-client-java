package io.getunleash;

import io.getunleash.lang.Nullable;
import java.util.List;

public class Segment {
    private int id;
    private String name;
    @Nullable private String description;
    private List<Constraint> constraints;
    private String createdBy;
    private String createdAt;

    public Segment(
            int id,
            String name,
            @Nullable String description,
            List<Constraint> constraints,
            String createdBy,
            String createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.constraints = constraints;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
