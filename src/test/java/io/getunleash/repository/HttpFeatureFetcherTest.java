package io.getunleash.repository;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.jenspiegsa.wiremockextension.WireMockSettings;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import io.getunleash.FeatureToggle;
import io.getunleash.util.UnleashConfig;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(WireMockExtension.class)
@WireMockSettings(failOnUnmatchedRequests = false)
public class HttpFeatureFetcherTest {

    @ConfigureWireMock Options options = wireMockConfig().dynamicPort();
    @InjectServer WireMockServer serverMock;
    HttpFeatureFetcher fetcher;

    @BeforeEach
    void setUp() {
        URI uri = null;
        try {
            uri = new URI("http://localhost:" + serverMock.port() + "/api/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        fetcher = HttpFeatureFetcher.init(config);
    }

    /*
    @Test
    public void uri_is_not_absoulute() throws URISyntaxException {
        URI badUri = new URI("notAbsolute");
        exception.expectMessage("Invalid unleash repository uri [notAbsolute]");
        exception.expect(UnleashException.class);
        new HttpToggleFetcher(badUri);
    }

    @Test
    public void given_malformed_url_should_give_exception() throws URISyntaxException {
        String unknownProtocolUrl = "foo://bar";
        URI badUrl = new URI(unknownProtocolUrl);
        exception.expectMessage("Invalid unleash repository uri [" + unknownProtocolUrl + "]");
        exception.expect(UnleashException.class);
        new HttpToggleFetcher(badUrl);
    }
    */

    @Test
    public void happy_path_test_with_variants_and_segments() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v2-with-segments.json")));

        ClientFeaturesResponse response = fetcher.fetchFeatures();
        FeatureToggle featureX = response.getToggleCollection().getToggle("featureX");

        assertThat(featureX.isEnabled()).isTrue();

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void should_include_etag_in_second_request() throws URISyntaxException {

        // First fetch
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("ETag", "AZ12")
                                        .withBodyFile("features-v2-with-segments.json")));

        // Second fetch
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("If-None-Match", equalTo("AZ12"))
                        .willReturn(
                                aResponse()
                                        .withStatus(304)
                                        .withHeader("Content-Type", "application/json")));

        ClientFeaturesResponse response1 = fetcher.fetchFeatures();
        ClientFeaturesResponse response2 = fetcher.fetchFeatures();

        assertThat(response1.getStatus()).isEqualTo(ClientFeaturesResponse.Status.CHANGED);
        assertThat(response2.getStatus()).isEqualTo(ClientFeaturesResponse.Status.NOT_CHANGED);
    }

    @Test
    @ExtendWith(UnleashExceptionExtension.class)
    public void given_empty_body() throws URISyntaxException {
        serverMock.resetAll();
        ;
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")));

        fetcher.fetchFeatures();

        serverMock.verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    @ExtendWith(UnleashExceptionExtension.class)
    public void given_json_without_feature_field() throws Exception {

        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{}")));

        fetcher.fetchFeatures();

        serverMock.verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void should_handle_not_changed() throws URISyntaxException {
        serverMock.resetAll();
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(304)
                                        .withHeader("Content-Type", "application/json")));

        ClientFeaturesResponse response = fetcher.fetchFeatures();
        assertThat(response.getStatus()).isEqualTo(ClientFeaturesResponse.Status.NOT_CHANGED);

        serverMock.verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @ParameterizedTest
    @ValueSource(
            ints = {
                HttpURLConnection.HTTP_MOVED_PERM,
                HttpURLConnection.HTTP_MOVED_TEMP,
                HttpURLConnection.HTTP_SEE_OTHER
            })
    public void should_handle_redirect(int responseCode) throws URISyntaxException {
        serverMock.resetAll();
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(responseCode)
                                        .withHeader(
                                                "Location",
                                                "http://localhost:"
                                                        + serverMock.port()
                                                        + "/api/v2/client/features")));
        serverMock.stubFor(
                get(urlEqualTo("/api/v2/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v2-with-segments.json")));

        ClientFeaturesResponse response = fetcher.fetchFeatures();
        assertThat(response.getStatus()).isEqualTo(ClientFeaturesResponse.Status.CHANGED);

        serverMock.verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
        serverMock.verify(
                getRequestedFor(urlMatching("/api/v2/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 500, 503})
    public void should_handle_errors(int httpCode) throws URISyntaxException {
        serverMock.resetAll();
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(httpCode)
                                        .withHeader("Content-Type", "application/json")));

        ClientFeaturesResponse response = fetcher.fetchFeatures();
        assertThat(response.getStatus()).isEqualTo(ClientFeaturesResponse.Status.UNAVAILABLE);
        assertThat(response.getHttpStatusCode()).isEqualTo(httpCode);

        serverMock.verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void should_not_set_empty_ifNoneMatchHeader() throws URISyntaxException {
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v2-with-segments.json")));

        fetcher.fetchFeatures();

        serverMock.verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withoutHeader("If-None-Match"));
    }

    @Test
    public void should_notify_location_on_error() throws URISyntaxException {
        serverMock.resetAll();
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(308)
                                        .withHeader("Location", "https://unleash.com")));

        ClientFeaturesResponse response = fetcher.fetchFeatures();
        assertThat(response.getLocation()).isEqualTo("https://unleash.com");
    }

    @Test
    public void should_add_project_filter_to_toggles_url_if_config_has_it_set()
            throws URISyntaxException {
        serverMock.resetAll();
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features?project=name"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v2-with-segments.json")));
        fetcher.fetchFeatures();
        serverMock.verify(getRequestedFor(urlMatching("/api/client/features\\?project=name")));
    }

    @Test
    public void should_throw_an_exception_if_project_name_is_not_url_friendly()
            throws URISyntaxException {
        URI uri = new URI(serverMock.baseUrl() + "/api/");
        String name = "^!#$!$?";
        UnleashConfig config =
                UnleashConfig.builder().appName("test").unleashAPI(uri).projectName(name).build();
        assertThatThrownBy(() -> new HttpToggleFetcher(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("?project=" + name + "] was not URL friendly.");
    }
}
