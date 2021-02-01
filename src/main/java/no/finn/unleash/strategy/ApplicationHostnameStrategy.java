package no.finn.unleash.strategy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class ApplicationHostnameStrategy implements Strategy {
    public static final String HOST_NAMES_PARAM = "hostNames";
    protected final String NAME = "applicationHostname";
    private final String hostname;

    public ApplicationHostnameStrategy() {
        this.hostname = resolveHostname();
    }

    private String resolveHostname() {
        String hostname = System.getProperty("hostname");
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname = "undefined";
            }
        }
        return hostname;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isEnabled(Map<String, String> parameters) {
        return Optional.ofNullable(parameters.get(HOST_NAMES_PARAM))
                .map(hostString -> hostString.toLowerCase())
                .map(hostString -> Arrays.asList(hostString.split(",\\s*")))
                .map(hostList -> hostList.contains(hostname.toLowerCase()))
                .orElse(false);
    }
}
