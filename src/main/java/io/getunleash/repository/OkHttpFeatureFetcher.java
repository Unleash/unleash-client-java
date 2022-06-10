package io.getunleash.repository;

import static io.getunleash.util.UnleashConfig.UNLEASH_APP_NAME_HEADER;
import static io.getunleash.util.UnleashConfig.UNLEASH_INSTANCE_ID_HEADER;

import com.google.gson.JsonSyntaxException;
import io.getunleash.UnleashException;
import io.getunleash.util.UnleashConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpFeatureFetcher implements FeatureFetcher {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration CALL_TIMEOUT = Duration.ofSeconds(5);

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
                        .connectTimeout(CONNECT_TIMEOUT)
                        .callTimeout(CALL_TIMEOUT)
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
        this.client = configureInterceptor(unleashConfig, builder.build());
    }

    public OkHttpFeatureFetcher(UnleashConfig unleashConfig, OkHttpClient client) {
        this.client = configureInterceptor(unleashConfig, client);
        this.toggleUrl =
                Objects.requireNonNull(
                        HttpUrl.get(
                                unleashConfig
                                        .getUnleashURLs()
                                        .getFetchTogglesURL(
                                                unleashConfig.getProjectName(),
                                                unleashConfig.getNamePrefix())));
    }

    public OkHttpClient configureInterceptor(UnleashConfig config, OkHttpClient client) {
        return client.newBuilder()
                .addInterceptor(
                        (c) -> {
                            Request.Builder headers =
                                    c.request()
                                            .newBuilder()
                                            .addHeader("Content-Type", "application/json")
                                            .addHeader("Accept", "application/json")
                                            .addHeader(UNLEASH_APP_NAME_HEADER, config.getAppName())
                                            .addHeader(
                                                    UNLEASH_INSTANCE_ID_HEADER,
                                                    config.getInstanceId())
                                            .addHeader("User-Agent", config.getAppName())
                                            .addHeader(
                                                    "Unleash-Client-Spec",
                                                    config.getClientSpecificationVersion());
                            for (Map.Entry<String, String> headerEntry :
                                    config.getCustomHttpHeaders().entrySet()) {
                                headers =
                                        headers.addHeader(
                                                headerEntry.getKey(), headerEntry.getValue());
                            }
                            for (Map.Entry<String, String> headerEntry :
                                    config.getCustomHttpHeadersProvider()
                                            .getCustomHeaders()
                                            .entrySet()) {
                                headers =
                                        headers.addHeader(
                                                headerEntry.getKey(), headerEntry.getValue());
                            }
                            return c.proceed(headers.build());
                        })
                .build();
    }

    @Override
    public ClientFeaturesResponse fetchFeatures() throws UnleashException {
        Request request = new Request.Builder().url(toggleUrl).get().build();
        HttpUrl location = toggleUrl;
        int code = 200;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                location = response.request().url();
                code = response.code();
                if (response.cacheResponse() != null) {
                    return new ClientFeaturesResponse(
                            ClientFeaturesResponse.Status.NOT_CHANGED, 304);
                } else {
                    FeatureCollection features =
                            JsonFeatureParser.fromJson(response.body().charStream());
                    return new ClientFeaturesResponse(
                            ClientFeaturesResponse.Status.CHANGED,
                            features.getToggleCollection(),
                            features.getSegmentCollection());
                }
            } else if (response.code() >= 301 && response.code() <= 304) {
                return new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.NOT_CHANGED, response.code());
            } else {
                return new ClientFeaturesResponse(
                        ClientFeaturesResponse.Status.UNAVAILABLE, response.code());
            }
        } catch (IOException ioEx) {
            throw new UnleashException("Could not fetch toggles", ioEx);
        } catch (IllegalStateException | JsonSyntaxException ex) {
            return new ClientFeaturesResponse(
                    ClientFeaturesResponse.Status.UNAVAILABLE, code, location.toString());
        }
    }
}
