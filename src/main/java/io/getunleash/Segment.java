package io.getunleash;

import io.getunleash.lang.Nullable;
import java.util.List;

public class Segment {
    private int id;
    private String name;
    private List<Constraint> constraints;

    public Segment(
            int id,
            String name,
            List<Constraint> constraints){
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
}
