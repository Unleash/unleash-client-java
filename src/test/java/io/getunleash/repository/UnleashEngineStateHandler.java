package io.getunleash.repository;

import io.getunleash.DefaultUnleash;
import io.getunleash.FeatureToggle;
import io.getunleash.engine.UnleashEngine;
import io.getunleash.engine.YggdrasilInvalidInputException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

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

    public void setState(FeatureToggle ...featureToggles) {
        FeatureCollection madeUp = new FeatureCollection(
            new ToggleCollection(Arrays.asList(featureToggles)),
            new SegmentCollection(Collections.emptyList())
        );
        setState(madeUp);
    }

    public void setState(FeatureCollection madeUp) {
        setState(JsonFeatureParser.toJsonString(madeUp));
    }

    public void setState(String raw) {
        try {

            this.unleashEngine.takeState(raw);
        } catch (YggdrasilInvalidInputException e) {
            throw new RuntimeException(e);
        }
    }
}
