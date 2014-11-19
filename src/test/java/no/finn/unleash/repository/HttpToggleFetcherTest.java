package no.finn.unleash.repository;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.UnleashException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class HttpToggleFetcherTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Test
    public void uriIsNotAbsoulute() throws URISyntaxException {
        URI badUri = new URI("notAbsolute");
        try {
            new HttpToggleFetcher(badUri);
            fail("Should give IllegalArgumentException");
        } catch (UnleashException e) {
            assertTrue("Expected IllegalArgumentException",e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void givenMalformedUrlShouldGiveException() throws URISyntaxException {
        String unknownProtocolUrl = "foo://bar";
        URI badUrl = new URI(unknownProtocolUrl);
        try {
            new HttpToggleFetcher(badUrl);
            fail("Should give MalformedURLException");
        } catch (UnleashException e) {
            assertTrue("Expected MalformedURLException", e.getCause() instanceof MalformedURLException);
            assertTrue("Exception message should contain URI, got:" + e.getMessage(), e.getMessage().contains(unknownProtocolUrl));
        }
    }

    @Test
    public void exampleTest() throws URISyntaxException {

        stubFor(get(urlEqualTo("/features"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("features.json")));

        URI uri = new URI("http://localhost:"+wireMockRule.port()+ "/features");
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(uri);
        Response response = httpToggleFetcher.fetchToggles();
        FeatureToggle featureX = response.getToggleCollection().getToggle("featureX");

        assertTrue(featureX.isEnabled());


        verify(getRequestedFor(urlMatching("/features"))
                .withHeader("Content-Type", matching("application/json")));
    }



}