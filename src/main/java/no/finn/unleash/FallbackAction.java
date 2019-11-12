package no.finn.unleash;

public interface FallbackAction {
    void apply(String toggleName, UnleashContext unleashContext);
}
