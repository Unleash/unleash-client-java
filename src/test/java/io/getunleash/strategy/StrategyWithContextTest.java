package io.getunleash.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import io.getunleash.UnleashContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StrategyWithContextTest {
    private StrategyUsingContext strategy;

    @BeforeEach
    public void setup() {
        strategy = new StrategyUsingContext();
    }

    @Test
    public void should_be_enabled_for_known_user() {
        // Params
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123");

        // Context
        UnleashContext context = UnleashContext.builder().userId("123").build();

        assertTrue(strategy.isEnabled(params, context));
    }

    @Test
    public void should_not_enabled_for_unknown_user() {
        // Params
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123");

        // Context
        UnleashContext context = UnleashContext.builder().userId("other").build();

        assertFalse(strategy.isEnabled(params, context));
    }
}
