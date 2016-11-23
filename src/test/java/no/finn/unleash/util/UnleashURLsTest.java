package no.finn.unleash.util;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UnleashURLsTest {


    @Test
    public void should_handle_additional_slash() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/"));
        assertThat(urls.getFetchTogglesURL().toString(), is("http://unleash.com/features"));
    }

    @Test
    public void should_set_correct_client_register_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com"));
        assertThat(urls.getClientRegisterURL().toString(), is("http://unleash.com/client/register"));
    }

    @Test
    public void should_set_correct_client_metrics_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com"));
        assertThat(urls.getClientMetricsURL().toString(), is("http://unleash.com/client/metrics"));
    }

    @Test
    public void should_set_correct_fetch_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com"));
        assertThat(urls.getFetchTogglesURL().toString(), is("http://unleash.com/features"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw() {
        new UnleashURLs(URI.create("unleash.com"));
    }
}