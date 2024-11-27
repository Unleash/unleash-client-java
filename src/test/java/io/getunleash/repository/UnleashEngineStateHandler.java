package io.getunleash.repository;

import io.getunleash.DefaultUnleash;
import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import io.getunleash.engine.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnleashEngineStateHandler {
    private final UnleashEngine unleashEngine;

    public UnleashEngineStateHandler(DefaultUnleash unleash) {
        // Access the private field
        Field field = null;
        try {
            field = DefaultUnleash.class.getDeclaredField("unleashEngine");
            field.setAccessible(true); // Bypass the "private" access modifier
            // Get value
            unleashEngine = (UnleashEngine) field.get(unleash);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setState(FeatureToggle... featureToggles) {
        FeatureCollection madeUp =
                new FeatureCollection(
                        new ToggleCollection(Arrays.asList(featureToggles)),
                        new SegmentCollection(Collections.emptyList()));
        setState(madeUp);
    }

    public void setState(List<FeatureToggle> featureToggles, List<Segment> segments) {
        FeatureCollection madeUp =
                new FeatureCollection(
                        new ToggleCollection(featureToggles), new SegmentCollection(segments));
        setState(madeUp);
    }

    public void setState(FeatureCollection madeUp) {
        setState(JsonFeatureParser.toJsonString(madeUp));
    }

    public MetricsBucket captureMetrics() {
        try {
            return this.unleashEngine.getMetrics();
        } catch (YggdrasilError e) {
            throw new RuntimeException(e);
        }
    }

    public void setState(String raw) {
        try {
            this.unleashEngine.takeState(raw);
        } catch (YggdrasilInvalidInputException e) {
            throw new RuntimeException(e);
        }
    }
}
