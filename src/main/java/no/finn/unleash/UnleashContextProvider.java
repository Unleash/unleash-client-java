package no.finn.unleash;

public interface UnleashContextProvider {
    UnleashContext getContext();

    static UnleashContextProvider getDefaultProvider() {
        return () -> UnleashContext.builder().build();
    }
}
