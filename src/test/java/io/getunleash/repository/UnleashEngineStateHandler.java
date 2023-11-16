package io.getunleash.repository;

import io.getunleash.FeatureToggle;
import io.getunleash.engine.UnleashEngine;
import io.getunleash.engine.YggdrasilInvalidInputException;

import java.util.Collections;
import java.util.Optional;

public class UnleashEngineStateHandler {
    private final UnleashEngine unleashEngine;

    public UnleashEngineStateHandler(Optional<UnleashEngine> unleashEngine) {
        this.unleashEngine = unleashEngine.orElseThrow(() -> new IllegalStateException("Missing UnleashEngine"));
    }

    public void setState(FeatureToggle featureToggle) {
        FeatureCollection madeUp = new FeatureCollection(
            new ToggleCollection(Collections.singleton(featureToggle)),
            new SegmentCollection(Collections.emptyList())
        );
        setState(madeUp);
    }

    public void setState(FeatureCollection madeUp) {
        try {
            this.unleashEngine.takeState(JsonFeatureParser.toJsonString(madeUp));
        } catch (YggdrasilInvalidInputException e) {
            throw new RuntimeException(e);
        }
    }
}
