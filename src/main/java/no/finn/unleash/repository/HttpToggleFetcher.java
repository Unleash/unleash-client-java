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

public final class HttpToggleFetcher implements ToggleFetcher {
    private static final int CONNECT_TIMEOUT = 10000;
    private Optional<String> etag = Optional.empty();

    private final URL toggleUrl;
    private UnleashConfig unleashConfig;

    public HttpToggleFetcher(UnleashConfig unleashConfig) {
        this.unleashConfig = unleashConfig;
        this.toggleUrl = unleashConfig.getUnleashURLs().getFetchTogglesURL();
    }

    @Override
    public FeatureToggleResponse fetchToggles() throws UnleashException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) toggleUrl.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(CONNECT_TIMEOUT);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            UnleashConfig.setRequestProperties(connection, this.unleashConfig);

            if(etag.isPresent()) {
                connection.setRequestProperty("If-None-Match", etag.get());
            }

            connection.setUseCaches(true);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode < 300) {
                return getToggleResponse(connection);
            } else if (responseCode == 304) {
                return new FeatureToggleResponse(FeatureToggleResponse.Status.NOT_CHANGED, responseCode);
            } else {
                return new FeatureToggleResponse(FeatureToggleResponse.Status.UNAVAILABLE, responseCode, getLocationHeader(connection));
            }
        } catch (IOException e) {
            throw new UnleashException("Could not fetch toggles", e);
        } catch (IllegalStateException e) {
            throw new UnleashException(e.getMessage(), e);
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
    }

    private FeatureToggleResponse getToggleResponse(HttpURLConnection request) throws IOException {
        etag = Optional.ofNullable(request.getHeaderField("ETag"));

        try(BufferedReader reader = new BufferedReader(
                new InputStreamReader((InputStream) request.getContent(), StandardCharsets.UTF_8))) {

            ToggleCollection toggles = JsonToggleParser.fromJson(reader);
            return new FeatureToggleResponse(FeatureToggleResponse.Status.CHANGED, toggles);
        }
    }

    private String getLocationHeader(HttpURLConnection connection) {
        return connection.getHeaderField("Location");
    }
}
