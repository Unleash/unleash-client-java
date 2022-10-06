package io.getunleash.repository;

import com.google.gson.JsonSyntaxException;
import io.getunleash.UnleashException;
import io.getunleash.util.OkHttpClientConfigurer;
import io.getunleash.util.UnleashConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpFeatureFetcher implements FeatureFetcher {
    private final HttpUrl toggleUrl;
    private final OkHttpClient client;

    public OkHttpFeatureFetcher(UnleashConfig unleashConfig) {
        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory("http_cache").toFile();
        } catch (IOException ignored) {
        }
        OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                        .connectTimeout(unleashConfig.getFetchTogglesConnectTimeout())
                        .callTimeout(unleashConfig.getFetchTogglesReadTimeout())
                        .followRedirects(true);
        if (tempDir != null) {
            builder = builder.cache(new Cache(tempDir, 1024 * 1024 * 50));
        }
        if (unleashConfig.getProxy() != null) {
            builder = builder.proxy(unleashConfig.getProxy());
        }

        this.toggleUrl =
                Objects.requireNonNull(
                        HttpUrl.get(
                                unleashConfig
                                        .getUnleashURLs()
                                        .getFetchTogglesURL(
                                                unleashConfig.getProjectName(),
                                                unleashConfig.getNamePrefix())));
        this.client = OkHttpClientConfigurer.configureInterceptor(unleashConfig, builder.build());
    }

    public OkHttpFeatureFetcher(UnleashConfig unleashConfig, OkHttpClient client) {
        this.client = OkHttpClientConfigurer.configureInterceptor(unleashConfig, client);
        this.toggleUrl =
                Objects.requireNonNull(
                        HttpUrl.get(
                                unleashConfig
                                        .getUnleashURLs()
                                        .getFetchTogglesURL(
                                                unleashConfig.getProjectName(),
                                                unleashConfig.getNamePrefix())));
    }

    @Override
    public ClientFeaturesResponse fetchFeatures() throws UnleashException {
        Request request = new Request.Builder().url(toggleUrl).get().build();
        HttpUrl location = toggleUrl;
        int code = 200;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                if (response.networkResponse() != null
                        && response.networkResponse().code() == 304) {
                    return new ClientFeaturesResponse(
                            ClientFeaturesResponse.Status.NOT_CHANGED, 304);
                }
                location = response.request().url();
                code = response.code();
                FeatureCollection features =
                        JsonFeatureParser.fromJson(
                                Objects.requireNonNull(response.body()).charStream());
                return new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.CHANGED,
                        features.getToggleCollection(),
                        features.getSegmentCollection());
            } else if (response.code() == 304) {
                return new ClientFeaturesResponse(FeatureToggleResponse.Status.NOT_CHANGED, response.code());
            } else {
                return new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.UNAVAILABLE, response.code());
            }
        } catch (IOException | NullPointerException ioEx) {
            throw new UnleashException("Could not fetch toggles", ioEx);
        } catch (IllegalStateException | JsonSyntaxException ex) {
            return new ClientFeaturesResponse(
                    ClientFeaturesResponse.Status.UNAVAILABLE, code, location.toString());
        }
    }
}
