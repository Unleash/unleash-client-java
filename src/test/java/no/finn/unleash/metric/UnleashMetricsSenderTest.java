package no.finn.unleash.metric;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.finn.unleash.util.UnleashConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashSet;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class UnleashMetricsSenderTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void should_send_client_registration() throws URISyntaxException {
        stubFor(post(urlEqualTo("/client/register"))
                .withHeader("UNLEASH-APPNAME", matching("test-app"))
                .willReturn(aResponse().withStatus(200)));

        URI uri = new URI("http://localhost:"+wireMockRule.port());
        UnleashConfig config = UnleashConfig.builder().appName("test-app").unleashAPI(uri).build();

        UnleashMetricsSender sender = new UnleashMetricsSender(config);
        sender.registerClient(new ClientRegistration(config, LocalDateTime.now(), new HashSet<String>()));

        verify(postRequestedFor(urlMatching("/client/register"))
                .withRequestBody(matching(".*appName.*"))
                .withRequestBody(matching(".*strategies.*"))
                .withHeader("UNLEASH-APPNAME", matching("test-app")));
    }

    @Test
    public void should_send_client_metrics() throws URISyntaxException {
        stubFor(post(urlEqualTo("/client/metrics"))
                .withHeader("UNLEASH-APPNAME", matching("test-app"))
                .willReturn(aResponse().withStatus(200)));

        URI uri = new URI("http://localhost:"+wireMockRule.port());
        UnleashConfig config = UnleashConfig.builder().appName("test-app").unleashAPI(uri).build();

        UnleashMetricsSender sender = new UnleashMetricsSender(config);
        MetricsBucket bucket = new MetricsBucket();
        ClientMetrics metrics = new ClientMetrics(config, bucket);
        sender.sendMetrics(metrics);

        verify(postRequestedFor(urlMatching("/client/metrics"))
                .withRequestBody(matching(".*appName.*"))
                .withRequestBody(matching(".*bucket.*"))
                .withHeader("UNLEASH-APPNAME", matching("test-app")));
    }

    @Test
    public void should_handle_service_failure_when_sending_metrics() throws URISyntaxException {
        stubFor(post(urlEqualTo("/client/metrics"))
                .withHeader("UNLEASH-APPNAME", matching("test-app"))
                .willReturn(aResponse().withStatus(500)));

        URI uri = new URI("http://localhost:"+wireMockRule.port());
        UnleashConfig config = UnleashConfig.builder().appName("test-app").unleashAPI(uri).build();

        UnleashMetricsSender sender = new UnleashMetricsSender(config);
        MetricsBucket bucket = new MetricsBucket();
        ClientMetrics metrics = new ClientMetrics(config, bucket);
        sender.sendMetrics(metrics);

        verify(postRequestedFor(urlMatching("/client/metrics"))
                .withRequestBody(matching(".*appName.*"))
                .withRequestBody(matching(".*bucket.*"))
                .withHeader("UNLEASH-APPNAME", matching("test-app")));
    }

}