package no.finn.unleash.repository;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.UnleashException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.net.URISyntaxException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

public class HttpToggleFetcherTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(0);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void uriIsNotAbsoulute() throws URISyntaxException {
        URI badUri = new URI("notAbsolute");
        exception.expectMessage("Invalid unleash repository uri [notAbsolute]");
        exception.expect(UnleashException.class);
        new HttpToggleFetcher(badUri);
    }

    @Test
    public void givenMalformedUrlShouldGiveException() throws URISyntaxException {
        String unknownProtocolUrl = "foo://bar";
        URI badUrl = new URI(unknownProtocolUrl);
        exception.expectMessage("Invalid unleash repository uri [" + unknownProtocolUrl + "]");
        exception.expect(UnleashException.class);
        new HttpToggleFetcher(badUrl);
    }

    @Test
    public void happyPathTest() throws URISyntaxException {
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

    @Test
    public void givenEmptyBody() throws URISyntaxException {
        stubFor(get(urlEqualTo("/features"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        URI uri = new URI("http://localhost:"+wireMockRule.port()+ "/features");
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(uri);
        exception.expect(UnleashException.class);
        httpToggleFetcher.fetchToggles();


        verify(getRequestedFor(urlMatching("/features"))
                .withHeader("Content-Type", matching("application/json")));
    }

    @Test
    public void shouldHandleNotChanged() throws URISyntaxException {
        stubFor(get(urlEqualTo("/features"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(302)
                        .withHeader("Content-Type", "application/json")));

        URI uri = new URI("http://localhost:"+wireMockRule.port()+ "/features");
        HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(uri);
        Response response = httpToggleFetcher.fetchToggles();
        assertEquals("Should return status NOT_CHANGED", response.getStatus(), Response.Status.NOT_CHANGED);

        verify(getRequestedFor(urlMatching("/features"))
                .withHeader("Content-Type", matching("application/json")));

    }

    @Test
    public void shouldHandleErrors() throws URISyntaxException {
        int httpCodes[] = {400,401,403,404,500,503};
        for(int httpCode:httpCodes) {
            stubFor(get(urlEqualTo("/features"))
                    .withHeader("Accept", equalTo("application/json"))
                    .willReturn(aResponse()
                            .withStatus(httpCode)
                            .withHeader("Content-Type", "application/json")));

            URI uri = new URI("http://localhost:" + wireMockRule.port() + "/features");
            HttpToggleFetcher httpToggleFetcher = new HttpToggleFetcher(uri);
            Response response = httpToggleFetcher.fetchToggles();
            assertEquals("Should return status NOT_CHANGED", response.getStatus(), Response.Status.NOT_CHANGED);

            verify(getRequestedFor(urlMatching("/features"))
                    .withHeader("Content-Type", matching("application/json")));
        }

    }

}