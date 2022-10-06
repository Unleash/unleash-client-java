package io.getunleash.util;

import static io.getunleash.util.UnleashConfig.UNLEASH_APP_NAME_HEADER;
import static io.getunleash.util.UnleashConfig.UNLEASH_INSTANCE_ID_HEADER;

import java.util.Map;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OkHttpClientConfigurer {
    public static OkHttpClient configureInterceptor(UnleashConfig config, OkHttpClient client) {
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
}
