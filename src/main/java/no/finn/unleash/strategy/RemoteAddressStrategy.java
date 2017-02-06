package no.finn.unleash.strategy;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.UnleashContext;

public final class RemoteAddressStrategy implements Strategy {

    protected static final String PARAM = "IPs";
    private static final String STRATEGY_NAME = "remoteAddress";


    @Override
    public String getName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return false;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters, UnleashContext context) {
        return Optional.ofNullable(parameters.get(PARAM))
                .map(ips -> Arrays.asList(ips.split(",\\s*")))
                .map(ips -> ips.contains(context.getRemoteAddress().orElse(null)))
                .orElse(false);
    }

}
