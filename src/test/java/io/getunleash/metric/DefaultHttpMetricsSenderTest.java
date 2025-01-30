package io.getunleash.metric;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.getunleash.engine.MetricsBucket;
import io.getunleash.util.UnleashConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class DefaultHttpMetricsSenderTest {

    @RegisterExtension
    static WireMockExtension serverMock =
            WireMockExtension.newInstance()
                    .configureStaticDsl(true)
                    .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
                    .build();

    @Test
    public void should_send_client_registration() throws URISyntaxException {
        stubFor(
                post(urlEqualTo("/client/register"))
                        .withHeader("X-UNLEASH-APPNAME", matching("test-app"))
                        .willReturn(aResponse().withStatus(200)));

        URI uri = new URI("http://localhost:" + serverMock.getPort());
        UnleashConfig config = UnleashConfig.builder().appName("test-app").unleashAPI(uri).build();

        DefaultHttpMetricsSender sender = new DefaultHttpMetricsSender(config);
        sender.registerClient(
                new ClientRegistration(config, LocalDateTime.now(), new HashSet<String>()));

        verify(
                postRequestedFor(urlMatching("/client/register"))
                        .withRequestBody(matching(".*appName.*"))
                        .withRequestBody(matching(".*strategies.*"))
                        .withHeader("X-UNLEASH-APPNAME", matching("test-app")));
    }

    @Test
    public void should_send_client_metrics() throws URISyntaxException {
        stubFor(
                post(urlEqualTo("/client/metrics"))
                        .withHeader("X-UNLEASH-APPNAME", matching("test-app"))
                        .willReturn(aResponse().withStatus(200)));

        URI uri = new URI("http://localhost:" + serverMock.getPort());
        UnleashConfig config = UnleashConfig.builder().appName("test-app").unleashAPI(uri).build();

        DefaultHttpMetricsSender sender = new DefaultHttpMetricsSender(config);
        MetricsBucket bucket = new MetricsBucket(Instant.now(), Instant.now(), null);
        ClientMetrics metrics = new ClientMetrics(config, bucket);
        sender.sendMetrics(metrics);

        verify(
                postRequestedFor(urlMatching("/client/metrics"))
                        .withRequestBody(matching(".*appName.*"))
                        .withRequestBody(matching(".*bucket.*"))
                        .withHeader("X-UNLEASH-APPNAME", matching("test-app")));
    }

    @Test
    public void should_handle_service_failure_when_sending_metrics() throws URISyntaxException {
        stubFor(
                post(urlEqualTo("/client/metrics"))
                        .withHeader("X-UNLEASH-APPNAME", matching("test-app"))
                        .willReturn(aResponse().withStatus(500)));

        URI uri = new URI("http://localhost:" + serverMock.getPort());
        UnleashConfig config = UnleashConfig.builder().appName("test-app").unleashAPI(uri).build();

        DefaultHttpMetricsSender sender = new DefaultHttpMetricsSender(config);
        MetricsBucket bucket = new MetricsBucket(Instant.now(), Instant.now(), null);
        ClientMetrics metrics = new ClientMetrics(config, bucket);
        sender.sendMetrics(metrics);

        verify(
                postRequestedFor(urlMatching("/client/metrics"))
                        .withRequestBody(matching(".*appName.*"))
                        .withRequestBody(matching(".*bucket.*"))
                        .withHeader("X-UNLEASH-APPNAME", matching("test-app")));
    }
}
