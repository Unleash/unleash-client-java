package io.getunleash.repository;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.getunleash.FeatureDefinition;
import io.getunleash.event.ClientFeaturesResponse;
import io.getunleash.util.UnleashConfig;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HttpFeatureFetcherTest {

    @RegisterExtension
    static WireMockExtension serverMock =
            WireMockExtension.newInstance()
                    .configureStaticDsl(true)
                    .options(wireMockConfig().dynamicPort())
                    .build();

    HttpFeatureFetcher fetcher;
    URI uri;

    @BeforeEach
    void setUp() {
        try {
            uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        fetcher = new HttpFeatureFetcher(config);
    }

    @Test
    public void happy_path_test_with_variants_and_segments() {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v2-with-segments.json")));

        ClientFeaturesResponse response = fetcher.fetchFeatures();

        Optional<FeatureDefinition> featureX =
                response.getFeatures().stream()
                        .filter(f -> f.getName().equals("featureX"))
                        .findFirst();

        assertThat(featureX).isPresent();

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void should_include_etag_in_second_request() {

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
    public void given_empty_body() {
        serverMock.resetAll();

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
    public void given_json_without_feature_field() {

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
    public void should_handle_not_changed() {
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
    public void should_handle_redirect(int responseCode) {
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
                                                        + serverMock.getPort()
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
    public void should_handle_errors(int httpCode) {
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
    public void should_not_set_empty_ifNoneMatchHeader() {
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
    public void should_notify_location_on_error() {
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
    public void should_add_project_filter_to_toggles_url_if_config_has_it_set() {
        serverMock.stubFor(
                get(urlEqualTo("/api/client/features?project=name"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v2-with-segments.json")));
        UnleashConfig config =
                UnleashConfig.builder().appName("test").unleashAPI(uri).projectName("name").build();

        fetcher = new HttpFeatureFetcher(config);
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
        assertThatThrownBy(() -> new HttpFeatureFetcher(config))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("?project=" + name + "] was not URL friendly.");
    }
}
