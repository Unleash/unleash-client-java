package no.finn.unleash.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

public class UnleashURLsTest {

    @Test
    public void should_handle_additional_slash() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL().toString())
                .isEqualTo("http://unleash.com/api/client/features");
    }

    @Test
    public void should_set_correct_client_register_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getClientRegisterURL().toString())
                .isEqualTo("http://unleash.com/api/client/register");
    }

    @Test
    public void should_set_correct_client_metrics_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getClientMetricsURL().toString())
                .isEqualTo("http://unleash.com/api/client/metrics");
    }

    @Test
    public void should_set_correct_fetch_url() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL().toString())
                .isEqualTo("http://unleash.com/api/client/features");
    }

    @Test
    public void should_set_build_fetch_url_if_project_and_prefix_are_null() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL(null, null).toString())
                .isEqualTo("http://unleash.com/api/client/features");
    }

    @Test
    public void should_set_build_fetch_url_with_project() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL("myProject", null).toString())
                .isEqualTo("http://unleash.com/api/client/features?project=myProject");
    }

    @Test
    public void should_set_build_fetch_url_with_nameprefix() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL(null, "prefix.").toString())
                .isEqualTo("http://unleash.com/api/client/features?namePrefix=prefix.");
    }

    @Test
    public void should_set_build_fetch_url_with_project_and_nameprefix() {
        UnleashURLs urls = new UnleashURLs(URI.create("http://unleash.com/api/"));
        assertThat(urls.getFetchTogglesURL("aproject", "prefix.").toString())
                .isEqualTo(
                        "http://unleash.com/api/client/features?project=aproject&namePrefix=prefix.");
    }

    @Test()
    public void should_throw() {
        assertThrows(
                IllegalArgumentException.class, () -> new UnleashURLs(URI.create("unleash.com")));
    }
}
