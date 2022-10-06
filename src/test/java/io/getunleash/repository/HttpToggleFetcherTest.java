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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.getunleash.FeatureToggle;
import io.getunleash.util.UnleashConfig;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HttpToggleFetcherTest {

    @RegisterExtension
    static WireMockExtension serverMock =
            WireMockExtension.newInstance()
                    .configureStaticDsl(true)
                    .options(wireMockConfig().dynamicPort())
                    .build();

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
    public void happy_path_test_version0() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v0.json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        FeatureToggle featureX = response.getToggleCollection().getToggle("featureX");

        assertThat(featureX.isEnabled()).isTrue();

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void happy_path_test_version1() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v1.json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        FeatureToggle featureX = response.getToggleCollection().getToggle("featureX");

        assertThat(featureX.isEnabled()).isTrue();

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void happy_path_test_version_with_variants() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v1-with-variants.json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        FeatureToggle featureX = response.getToggleCollection().getToggle("Test.variants");

        assertThat(featureX.isEnabled()).isTrue();
        assertThat(featureX.getVariants().get(0).getName()).isEqualTo("variant1");

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
                                        .withBodyFile("features-v1-with-variants.json")));

        // Second fetch
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("If-None-Match", equalTo("AZ12"))
                        .willReturn(
                                aResponse()
                                        .withStatus(304)
                                        .withHeader("Content-Type", "application/json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);

        FeatureToggleResponse response1 = httpToggleFetcher.fetchToggles();
        FeatureToggleResponse response2 = httpToggleFetcher.fetchToggles();

        assertThat(response1.getStatus()).isEqualTo(FeatureToggleResponse.Status.CHANGED);
        assertThat(response2.getStatus()).isEqualTo(FeatureToggleResponse.Status.NOT_CHANGED);
    }

    @Test
    @ExtendWith(UnleashExceptionExtension.class)
    public void given_empty_body() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        httpToggleFetcher.fetchToggles();

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    @ExtendWith(UnleashExceptionExtension.class)
    public void given_json_without_feature_field() throws Exception {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{}")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        httpToggleFetcher.fetchToggles();

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void should_handle_not_changed() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(304)
                                        .withHeader("Content-Type", "application/json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        assertThat(response.getStatus()).isEqualTo(FeatureToggleResponse.Status.NOT_CHANGED);

        verify(
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
        stubFor(
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
        stubFor(
                get(urlEqualTo("/api/v2/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(HttpURLConnection.HTTP_OK)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v1.json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        assertThat(response.getStatus()).isEqualTo(FeatureToggleResponse.Status.CHANGED);

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
        verify(
                getRequestedFor(urlMatching("/api/v2/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 404, 500, 503})
    public void should_handle_errors(int httpCode) throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(httpCode)
                                        .withHeader("Content-Type", "application/json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        assertThat(response.getStatus()).isEqualTo(FeatureToggleResponse.Status.UNAVAILABLE);
        assertThat(response.getHttpStatusCode()).isEqualTo(httpCode);

        verify(
                getRequestedFor(urlMatching("/api/client/features"))
                        .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void should_not_set_empty_ifNoneMatchHeader() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v1.json")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();

        verify(getRequestedFor(urlMatching("/api/client/features")).withoutHeader("If-None-Match"));
    }

    @Test
    public void should_notify_location_on_error() throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(308)
                                        .withHeader("Location", "https://unleash.com")));

        URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
        UnleashConfig config = UnleashConfig.builder().appName("test").unleashAPI(uri).build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        assertThat(response.getLocation()).isEqualTo("https://unleash.com");
    }

    @Test
    public void should_add_project_filter_to_toggles_url_if_config_has_it_set()
            throws URISyntaxException {
        stubFor(
                get(urlEqualTo("/api/client/features?project=name"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBodyFile("features-v1.json")));
        URI uri = new URI(serverMock.baseUrl() + "/api/");
        UnleashConfig config =
                UnleashConfig.builder().appName("test").unleashAPI(uri).projectName("name").build();
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(config);
        FeatureToggleResponse response = httpToggleFetcher.fetchToggles();
        verify(getRequestedFor(urlMatching("/api/client/features\\?project=name")));
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
