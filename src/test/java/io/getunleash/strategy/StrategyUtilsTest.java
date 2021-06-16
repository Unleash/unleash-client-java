package io.getunleash.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StrategyUtilsTest {

    @Test
    public void normalized_values_are_the_same_across_node_java_and_go_clients() {
        assertEquals(73, StrategyUtils.getNormalizedNumber("123", "gr1"));
        assertEquals(25, StrategyUtils.getNormalizedNumber("999", "groupX"));
    }
}
