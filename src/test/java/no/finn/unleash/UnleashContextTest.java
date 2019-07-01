package no.finn.unleash;


import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UnleashContextTest {

    @Test
    public void should_generate_default_context() {
        UnleashContext context = UnleashContext.builder().build();
        assertThat(context.getUserId().isPresent(), is(false));
        assertThat(context.getSessionId().isPresent(), is(false));
        assertThat(context.getRemoteAddress().isPresent(), is(false));
        assertThat(context.getProperties().size(), is(0));
    }

    @Test
    public void should_get_context_with_userId() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .build();
        assertThat(context.getUserId().get(), is("test@mail.com"));
    }

    @Test
    public void should_get_context_with_fields_set() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .sessionId("123")
                .remoteAddress("127.0.0.1")
                .environment("prod")
                .appName("myapp")
                .addProperty("test", "me")
                .build();

        assertThat(context.getUserId().get(), is("test@mail.com"));
        assertThat(context.getSessionId().get(), is("123"));
        assertThat(context.getRemoteAddress().get(), is("127.0.0.1"));
        assertThat(context.getEnvironment().get(), is("prod"));
        assertThat(context.getAppName().get(), is("myapp"));
        assertThat(context.getProperties().get("test"), is("me"));
    }

    @Test
    public void should_apply_context_fields() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .sessionId("123")
                .remoteAddress("127.0.0.1")
                .addProperty("test", "me")
                .build();

        UnleashConfig config = UnleashConfig.builder()
                .unleashAPI("http://test.com")
                .appName("someApp")
                .environment("stage")
                .build();

        UnleashContext enhanced = context.applyStaticFields(config);

        assertThat(enhanced.getUserId().get(), is("test@mail.com"));
        assertThat(enhanced.getSessionId().get(), is("123"));
        assertThat(enhanced.getRemoteAddress().get(), is("127.0.0.1"));

        assertThat(enhanced.getEnvironment().get(), is("stage"));
        assertThat(enhanced.getAppName().get(), is("someApp"));
    }

    @Test
    public void should_not_ovveride_static_context_fields() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .sessionId("123")
                .remoteAddress("127.0.0.1")
                .environment("env")
                .appName("myApp")
                .addProperty("test", "me")
                .build();

        UnleashConfig config = UnleashConfig.builder()
                .unleashAPI("http://test.com")
                .appName("someApp")
                .environment("stage")
                .build();


        UnleashContext enhanced = context.applyStaticFields(config);

        assertThat(enhanced.getUserId().get(), is("test@mail.com"));
        assertThat(enhanced.getSessionId().get(), is("123"));
        assertThat(enhanced.getRemoteAddress().get(), is("127.0.0.1"));
        assertThat(enhanced.getEnvironment().get(), is("env"));
        assertThat(enhanced.getAppName().get(), is("myApp"));
    }

}
