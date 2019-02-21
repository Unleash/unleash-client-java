package no.finn.unleash.metric;

import com.google.gson.*;
import no.finn.unleash.UnleashException;
import no.finn.unleash.event.EventDispatcher;
import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashURLs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

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
        UnleashURLs urls =  unleashConfig.getUnleashURLs();
        this.clientMetricsURL = urls.getClientMetricsURL();
        this.clientRegistrationURL = urls.getClientRegisterURL();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new DateTimeSerializer())
                .create();
    }

    static class DateTimeSerializer implements JsonSerializer<LocalDateTime> {
        @Override
        public JsonElement serialize(
                LocalDateTime localDateTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(ISO_INSTANT.format(localDateTime.toInstant(ZoneOffset.UTC)));
        };
    }

    public void registerClient(ClientRegistration registration) {
        if(!unleashConfig.isDisableMetrics()) {
            try {
                post(clientRegistrationURL, registration);
                eventDispatcher.dispatch(registration);
            } catch(UnleashException ex) {
                eventDispatcher.dispatch(ex);
            }
        }
    }

    public void sendMetrics(ClientMetrics metrics) {
        if(!unleashConfig.isDisableMetrics()) {
            try {
                post(clientMetricsURL, metrics);
                eventDispatcher.dispatch(metrics);
            } catch(UnleashException ex) {
                eventDispatcher.dispatch(ex);
            }
        }
    }

    private int post(URL url, Object o) throws UnleashException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(CONNECT_TIMEOUT);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            UnleashConfig.setRequestProperties(connection, this.unleashConfig);
            connection.setUseCaches (false);
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
            if(connection != null) {
                connection.disconnect();
            }
        }
    }
}
