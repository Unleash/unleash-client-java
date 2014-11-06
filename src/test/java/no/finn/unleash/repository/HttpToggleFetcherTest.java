package no.finn.unleash.repository;

import no.finn.unleash.UnleashException;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class HttpToggleFetcherTest {

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



}