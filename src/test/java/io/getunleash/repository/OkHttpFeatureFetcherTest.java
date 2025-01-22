package io.getunleash.repository;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.extension.RegisterExtension;

public class OkHttpFeatureFetcherTest {
    @RegisterExtension
    static WireMockExtension serverMock =
            WireMockExtension.newInstance()
                    .configureStaticDsl(true)
                    .options(wireMockConfig().dynamicPort())
                    .build();

    //     @Test
    //     public void happy_path_test_version0() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v0.json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         ClientFeaturesResponse response = okHttpToggleFetcher.fetchFeatures();
    //         FeatureToggle featureX = response.getMessage().getToggle("featureX");

    //         assertThat(featureX.isEnabled()).isTrue();

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     public void happy_path_test_version1() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v1.json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpToggleFetcher.fetchFeatures();
    //         FeatureToggle featureX = response.getMessage().getToggle("featureX");

    //         assertThat(featureX.isEnabled()).isTrue();

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     public void happy_path_test_version_with_variants() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v1-with-variants.json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpToggleFetcher.fetchFeatures();
    //         FeatureToggle featureX = response.getMessage().getToggle("Test.variants");

    //         assertThat(featureX.isEnabled()).isTrue();
    //         assertThat(featureX.getVariants().get(0).getName()).isEqualTo("variant1");

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     public void should_include_etag_in_second_request() throws URISyntaxException {
    //         // First fetch
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withHeader("ETag", "AZ12")
    //                                         .withBodyFile("features-v1-with-variants.json")));

    //         // Second fetch
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("If-None-Match", equalTo("AZ12"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(304)
    //                                         .withHeader("Content-Type", "application/json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);

    //         FeatureToggleResponse response1 = okHttpToggleFetcher.fetchFeatures();
    //         FeatureToggleResponse response2 = okHttpToggleFetcher.fetchFeatures();
    //         verify(
    //                 1,
    //
    // getRequestedFor(urlEqualTo("/api/client/features")).withoutHeader("If-None-Match"));
    //         verify(
    //                 1,
    //                 getRequestedFor(urlEqualTo("/api/client/features"))
    //                         .withHeader("If-None-Match", equalTo("AZ12")));
    //         assertThat(response1.getStatus()).isEqualTo(FeatureToggleResponse.Status.CHANGED);
    //
    // assertThat(response2.getStatus()).isEqualTo(FeatureToggleResponse.Status.NOT_CHANGED);
    //     }

    //     @Test
    //     @ExtendWith(UnleashExceptionExtension.class)
    //     public void given_empty_body() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         okHttpToggleFetcher.fetchFeatures();

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     @ExtendWith(UnleashExceptionExtension.class)
    //     public void given_json_without_feature_field() throws Exception {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBody("{}")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         okHttpToggleFetcher.fetchFeatures();

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     public void should_handle_not_changed() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(304)
    //                                         .withHeader("Content-Type", "application/json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpToggleFetcher.fetchFeatures();
    //         assertThat(response.getStatus()).isEqualTo(FeatureToggleResponse.Status.NOT_CHANGED);

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @ParameterizedTest
    //     @ValueSource(
    //             ints = {
    //                 HttpURLConnection.HTTP_MOVED_PERM,
    //                 HttpURLConnection.HTTP_MOVED_TEMP,
    //                 HttpURLConnection.HTTP_SEE_OTHER
    //             })
    //     public void should_handle_redirect(int responseCode) throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(responseCode)
    //                                         .withHeader(
    //                                                 "Location",
    //                                                 "http://localhost:"
    //                                                         + serverMock.getPort()
    //                                                         + "/api/v2/client/features")));
    //         stubFor(
    //                 get(urlEqualTo("/api/v2/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(HttpURLConnection.HTTP_OK)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v1.json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpToggleFetcher.fetchFeatures();
    //         assertThat(response.getStatus()).isEqualTo(FeatureToggleResponse.Status.CHANGED);

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //         verify(
    //                 getRequestedFor(urlMatching("/api/v2/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @ParameterizedTest
    //     @ValueSource(ints = {400, 401, 403, 404, 500, 503})
    //     public void should_handle_errors(int httpCode) throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(httpCode)
    //                                         .withHeader("Content-Type", "application/json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpToggleFetcher.fetchFeatures();
    //         assertThat(response.getStatus()).isEqualTo(FeatureToggleResponse.Status.UNAVAILABLE);
    //         assertThat(response.getHttpStatusCode()).isEqualTo(httpCode);

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     public void should_not_set_empty_ifNoneMatchHeader() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v1.json")));

    //         URI uri = new URI("http://localhost:" + serverMock.getPort() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher okHttpToggleFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpToggleFetcher.fetchFeatures();

    //
    // verify(getRequestedFor(urlMatching("/api/client/features")).withoutHeader("If-None-Match"));
    //     }

    //     @Test
    //     public void should_add_project_filter_to_toggles_url_if_config_has_it_set()
    //             throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features?project=name"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v1.json")));
    //         URI uri = new URI(serverMock.baseUrl() + "/api/");
    //         UnleashConfig config =
    //
    // UnleashConfig.builder().appName("test").unleashAPI(uri).projectName("name").build();
    //         OkHttpFeatureFetcher okHttpFeatureFetcher = new OkHttpFeatureFetcher(config);
    //         FeatureToggleResponse response = okHttpFeatureFetcher.fetchFeatures();
    //         verify(getRequestedFor(urlMatching("/api/client/features\\?project=name")));
    //     }

    //     @Test
    //     public void happy_path_test_with_variants_and_segments() throws URISyntaxException {
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v2-with-segments.json")));
    //         URI uri = new URI(serverMock.baseUrl() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         OkHttpFeatureFetcher fetcher = new OkHttpFeatureFetcher(config);
    //         ClientFeaturesResponse response = fetcher.fetchFeatures();
    //         FeatureToggle featureX = response.getMessage().getToggle("featureX");

    //         assertThat(featureX.isEnabled()).isTrue();

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }

    //     @Test
    //     public void should_include_client_specification_version_in_header() throws
    // URISyntaxException {
    //         URI uri = new URI(serverMock.baseUrl() + "/api/");
    //         UnleashConfig config =
    // UnleashConfig.builder().appName("test").unleashAPI(uri).build();
    //         stubFor(
    //                 get(urlEqualTo("/api/client/features"))
    //                         .withHeader("Accept", equalTo("application/json"))
    //                         .withHeader(
    //                                 "Unleash-Client-Spec",
    //                                 equalTo(config.getClientSpecificationVersion()))
    //                         .willReturn(
    //                                 aResponse()
    //                                         .withStatus(200)
    //                                         .withHeader("Content-Type", "application/json")
    //                                         .withBodyFile("features-v2-with-segments.json")));
    //         OkHttpFeatureFetcher fetcher = new OkHttpFeatureFetcher(config);
    //         ClientFeaturesResponse response = fetcher.fetchFeatures();
    //         FeatureToggle featureX = response.getMessage().getToggle("featureX");

    //         assertThat(featureX.isEnabled()).isTrue();

    //         verify(
    //                 getRequestedFor(urlMatching("/api/client/features"))
    //                         .withHeader("Content-Type", matching("application/json")));
    //     }
}
