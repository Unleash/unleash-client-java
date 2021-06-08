package io.getunleash.strategy;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import io.getunleash.UnleashContext;
import io.getunleash.util.IpAddressMatcher;

public final class RemoteAddressStrategy implements Strategy {
    static final String PARAM = "IPs";
    private static final String STRATEGY_NAME = "remoteAddress";
    private static final Pattern SPLITTER = Pattern.compile(",");

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
                .map(ips -> Arrays.asList(SPLITTER.split(ips, -1)))
                .map(
                        ips ->
                                ips.stream()
                                        .flatMap(
                                                ipAddress ->
                                                        buildIpAddressMatcher(ipAddress)
                                                                .map(Stream::of)
                                                                .orElseGet(Stream::empty))
                                        .map(
                                                subnet ->
                                                        context.getRemoteAddress()
                                                                .map(subnet::matches)
                                                                .orElse(false))
                                        .anyMatch(Boolean.TRUE::equals))
                .orElse(false);
    }

    private Optional<IpAddressMatcher> buildIpAddressMatcher(String ipAddress) {
        try {
            return Optional.of(new IpAddressMatcher(ipAddress));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
