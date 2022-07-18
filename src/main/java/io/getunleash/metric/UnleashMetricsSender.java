package io.getunleash.metric;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.google.gson.*;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashURLs;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

public class UnleashMetricsSender {
    private static final int CONNECT_TIMEOUT = 1000;

    private final Gson gson;
    private final EventDispatcher eventDispatcher;
    private UnleashConfig unleashConfig;
    private final URL clientRegistrationURL;
    private final URL clientMetricsURL;

    public UnleashMetricsSender(UnleashConfig unleashConfig) {
        this.unleashConfig = unleashConfig;
        this.eventDispatcher = new EventDispatcher(unleashConfig);
        UnleashURLs urls = unleashConfig.getUnleashURLs();
        this.clientMetricsURL = urls.getClientMetricsURL();
        this.clientRegistrationURL = urls.getClientRegisterURL();

        this.gson =
                new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new DateTimeSerializer())
                        .registerTypeAdapter(AtomicLong.class, new AtomicLongSerializer())
                        .create();
    }

    static class DateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(
                LocalDateTime localDateTime,
                Type type,
                JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(ISO_INSTANT.format(localDateTime.toInstant(ZoneOffset.UTC)));
        }
    }

    static class AtomicLongSerializer implements JsonSerializer<AtomicLong> {

        @Override
        public JsonElement serialize(
                AtomicLong src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.get());
        }
    }

    public void registerClient(ClientRegistration registration) {
        if (!unleashConfig.isDisableMetrics()) {
            try {
                post(clientRegistrationURL, registration);
                eventDispatcher.dispatch(registration);
            } catch (UnleashException ex) {
                eventDispatcher.dispatch(ex);
            }
        }
    }

    public void sendMetrics(ClientMetrics metrics) {
        if (!unleashConfig.isDisableMetrics()) {
            try {
                post(clientMetricsURL, metrics);
                eventDispatcher.dispatch(metrics);
            } catch (UnleashException ex) {
                eventDispatcher.dispatch(ex);
            }
        }
    }

    private int post(URL url, Object o) throws UnleashException {

        HttpURLConnection connection = null;
        try {
            if (this.unleashConfig.getProxy() != null) {
                connection = (HttpURLConnection) url.openConnection(this.unleashConfig.getProxy());
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setConnectTimeout((int) unleashConfig.getSendMetricsConnectTimeout().toMillis());
            connection.setReadTimeout((int) unleashConfig.getSendMetricsReadTimeout().toMillis());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            UnleashConfig.setRequestProperties(connection, this.unleashConfig);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            gson.toJson(o, wr);
            wr.flush();
            wr.close();

            connection.connect();

            // TODO should probably check response code to detect errors?
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new UnleashException("Could not post to Unleash API", e);
        } catch (IllegalStateException e) {
            throw new UnleashException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
