package io.getunleash.util;

import io.getunleash.engine.UnleashEngine;
import io.getunleash.lang.Nullable;

import java.util.Optional;

public class UnleashEngineReference {
    @Nullable private UnleashEngine engine;

    public void set(UnleashEngine engine) {
        this.engine = engine;
    }

    public Optional<UnleashEngine> get() {
        return Optional.ofNullable(engine);
    }
}
