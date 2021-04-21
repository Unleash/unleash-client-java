package no.finn.unleash.util;

import no.finn.unleash.lang.Nullable;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UnleashURLs {
    private final URL fetchTogglesURL;
    private final URL clientMetricsURL;
    private final URL clientRegisterURL;

    public UnleashURLs(URI unleashAPI) {
        try {
            String unleashAPIstr = unleashAPI.toString();
            fetchTogglesURL = URI.create(unleashAPIstr + "/client/features").normalize().toURL();
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

    public URL getFetchTogglesURL(@Nullable String projectName, @Nullable String namePrefix) {
        StringBuilder suffix = new StringBuilder("");
        appendParam(suffix, "project", projectName);
        appendParam(suffix, "namePrefix", namePrefix);

        try {
            return URI.create(fetchTogglesURL + suffix.toString()).normalize().toURL();
        } catch (IllegalArgumentException|MalformedURLException e) {
            throw new IllegalArgumentException("fetchTogglesURL [" + fetchTogglesURL + suffix + "] was not URL friendly.", e);
        }
    }

    private void appendParam(StringBuilder suffix, String key, @Nullable String value) {
        if(value == null) return;
        if(suffix.length() == 0){
            suffix.append("?");
        } else {
            suffix.append("&");
        }
        suffix.append(key).append("=").append(value);
    }


}
