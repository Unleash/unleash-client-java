package no.finn.unleash.repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import no.finn.unleash.UnleashException;
import no.finn.unleash.util.UnleashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HttpToggleFetcher implements ToggleFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(HttpToggleFetcher.class);

    private static final int CONNECT_TIMEOUT = 10000;
    private Optional<String> etag = Optional.empty();

    private final URL toggleUrl;
    private final UnleashConfig unleashConfig;

    public HttpToggleFetcher(UnleashConfig unleashConfig) {
        this.unleashConfig = unleashConfig;
        if (unleashConfig.getProjectName() != null) {
            this.toggleUrl =
                    unleashConfig
                            .getUnleashURLs()
                            .getFetchTogglesURL(unleashConfig.getProjectName());
        } else if (unleashConfig.getNamePrefix() != null) {
            this.toggleUrl =
                    unleashConfig
                            .getUnleashURLs()
                            .getFetchTogglesURLWithNamePrefix(unleashConfig.getNamePrefix());
        } else {
            this.toggleUrl = unleashConfig.getUnleashURLs().getFetchTogglesURL();
        }
    }

    @Override
    public FeatureToggleResponse fetchToggles() throws UnleashException {
        HttpURLConnection connection = null;
        try {
            connection = openConnection(toggleUrl);
            connection.connect();

            return getToggleResponse(connection, true);
        } catch (IOException e) {
            throw new UnleashException("Could not fetch toggles", e);
        } catch (IllegalStateException e) {
            throw new UnleashException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private FeatureToggleResponse getToggleResponse(
            HttpURLConnection request, boolean followRedirect) throws IOException {
        int responseCode = request.getResponseCode();
        if (responseCode < 300) {
            etag = Optional.ofNullable(request.getHeaderField("ETag"));

            try (BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    (InputStream) request.getContent(), StandardCharsets.UTF_8))) {

                ToggleCollection toggles = JsonToggleParser.fromJson(reader);
                return new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggles);
            }
        } else if (followRedirect
                && (responseCode == HttpURLConnection.HTTP_MOVED_TEMP
                        || responseCode == HttpURLConnection.HTTP_MOVED_PERM
                        || responseCode == HttpURLConnection.HTTP_SEE_OTHER)) {
            return followRedirect(request);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
            return new FeatureToggleResponse(
                    FeatureToggleResponse.Status.NOT_CHANGED, responseCode);
        } else {
            return new FeatureToggleResponse(
                    FeatureToggleResponse.Status.UNAVAILABLE,
                    responseCode,
                    getLocationHeader(request));
        }
    }

    private FeatureToggleResponse followRedirect(HttpURLConnection request) throws IOException {
        String newUrl = getLocationHeader(request);

        request = openConnection(new URL(newUrl));
        request.connect();
        LOG.info(
                "Redirecting from {} to {}. Please consider to update your config.",
                toggleUrl,
                newUrl);

        return getToggleResponse(request, false);
    }

    private String getLocationHeader(HttpURLConnection connection) {
        return connection.getHeaderField("Location");
    }

    private HttpURLConnection openConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(CONNECT_TIMEOUT);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        UnleashConfig.setRequestProperties(connection, this.unleashConfig);

        etag.ifPresent(val -> connection.setRequestProperty("If-None-Match", val));

        connection.setUseCaches(true);

        return connection;
    }
}
