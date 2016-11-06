package no.finn.unleash.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class UnleashURLs {
    private final URL fetchTogglesURL;
    private final URL clientMetricsURL;
    private final URL clientRegisterURL;

    public UnleashURLs(URI unleashAPI) {
        try {
            String unleashAPIstr = unleashAPI.toString();
            fetchTogglesURL = URI.create(unleashAPIstr + "/features").normalize().toURL();
            clientMetricsURL = URI.create(unleashAPIstr + "/client/metrics").normalize().toURL();
            clientRegisterURL = URI.create(unleashAPIstr + "/client/register").normalize().toURL();

        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Unleash API is not a valid URL: " + unleashAPI);
        }
    }

    public URL getFetchTogglesURL() {
        return fetchTogglesURL;
    }

    public URL getClientMetricsURL() {
        return clientMetricsURL;
    }

    public URL getClientRegisterURL() {
        return clientRegisterURL;
    }
}
