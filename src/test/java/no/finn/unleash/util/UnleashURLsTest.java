package no.finn.unleash.util;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UnleashURLsTest {


    @Test
    public void should_handle_additional_slash() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL().toString()).isEqualTo("http://unleash.com/api/client/features");
    }

    @Test
    public void should_set_correct_client_register_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getClientRegisterURL().toString()).isEqualTo("http://unleash.com/api/client/register");
    }

    @Test
    public void should_set_correct_client_metrics_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getClientMetricsURL().toString()).isEqualTo("http://unleash.com/api/client/metrics");
    }

    @Test
    public void should_set_correct_fetch_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL().toString()).isEqualTo("http://unleash.com/api/client/features");
    }

    @Test()
    public void should_throw() {
        assertThrows(IllegalArgumentException.class, () -> new UnleashURLs(URI.create("unleash.com")));
    }
}