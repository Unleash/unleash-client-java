package no.finn.unleash.strategy;

import java.util.HashMap;
import java.util.Map;
import no.finn.unleash.UnleashContext;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StrategyWithContextTest {
    private StrategyUsingContext strategy;

    @Before
    public void setup() {
        strategy = new StrategyUsingContext();
    }

    @Test
    public void should_be_enabled_for_known_user() {
        //Params
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123");

        //Context
        UnleashContext context = UnleashContext.builder().userId("123").build();

        assertTrue(strategy.isEnabled(params, context));
    }

    @Test
    public void should_not_enabled_for_unknown_user() {
        //Params
        Map<String, String> params = new HashMap<>();
        params.put("userIds", "123");

        //Context
        UnleashContext context = UnleashContext.builder().userId("other").build();

        assertFalse(strategy.isEnabled(params, context));
    }

}