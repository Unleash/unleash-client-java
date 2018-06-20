package no.finn.unleash.metric;

import com.github.jenspiegsa.mockitoextension.ConfigureWireMock;
import com.github.jenspiegsa.mockitoextension.InjectServer;
import com.github.jenspiegsa.mockitoextension.WireMockExtension;
import com.github.jenspiegsa.mockitoextension.WireMockSettings;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashSet;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(WireMockExtension.class)
@WireMockSettings(failOnUnmatchedRequests = false)
public class UnleashMetricsSenderTest {

    @ConfigureWireMock
    Options options = wireMockConfig()
            .dynamicPort();

    @InjectServer
    WireMockServer serverMock;

    @Test
    public void should_send_client_registration() throws URISyntaxException {
        stubFor(post(urlEqualTo("/client/register"))
                .withHeader("UNLEASH-APPNAME", matching("test-app"))
                .willReturn(aResponse().withStatus(200)));

        URI uri = new URI("http://localhost:"+serverMock.port());
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

        URI uri = new URI("http://localhost:"+serverMock.port());
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

        URI uri = new URI("http://localhost:"+serverMock.port());
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