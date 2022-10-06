package io.getunleash;

import static java.util.Arrays.asList;

import java.util.Collections;
import java.util.List;

public class Segment {
    private int id;
    private String name;
    private List<Constraint> constraints;

    public Segment(int id, String name, List<Constraint> constraints) {
        this.id = id;
        this.name = name;
        this.constraints = constraints;
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

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints;
    }

    public static Segment DENY_SEGMENT =
            new Segment(
                    -9999,
                    "NON_EXISTING_SEGMENT_ID",
                    asList(
                            new Constraint(
                                    "non-existing-segment-id",
                                    Operator.IN,
                                    Collections.emptyList())));
}
