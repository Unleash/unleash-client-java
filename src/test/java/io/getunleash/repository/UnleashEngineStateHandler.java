package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.engine.UnleashEngine;
import io.getunleash.engine.YggdrasilInvalidInputException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class UnleashEngineStateHandler {
    private final UnleashEngine unleashEngine;

    public UnleashEngineStateHandler(Optional<UnleashEngine> unleashEngine) {
        this.unleashEngine = unleashEngine.orElseThrow(() -> new IllegalStateException("Missing UnleashEngine"));
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
