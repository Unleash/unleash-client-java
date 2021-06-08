package io.getunleash;

public interface UnleashContextProvider {
    UnleashContext getContext();

    static UnleashContextProvider getDefaultProvider() {
        return () -> UnleashContext.builder().build();
    }
}
