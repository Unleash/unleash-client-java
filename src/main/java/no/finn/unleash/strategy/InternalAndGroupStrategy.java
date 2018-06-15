package no.finn.unleash.strategy;

import java.util.List;
import java.util.Map;

import no.finn.unleash.UnleashContext;

public class InternalAndGroupStrategy {

    public boolean isEnabled(List<Strategy> group, Map<String, String> parameters, UnleashContext unleashContext) {
        return false;
    }
}
