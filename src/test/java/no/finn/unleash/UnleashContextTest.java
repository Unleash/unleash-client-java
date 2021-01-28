package no.finn.unleash;


import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class UnleashContextTest {

    @Test
    public void should_generate_default_context() {
        UnleashContext context = UnleashContext.builder().build();
        assertThat(context.getUserId()).isEmpty();
        assertThat(context.getSessionId()).isEmpty();
        assertThat(context.getRemoteAddress()).isEmpty();
        assertThat(context.getProperties()).isEmpty();
    }

    @Test
    public void should_get_context_with_userId() {
        UnleashContext context = UnleashContext.builder()
                .userId("test@mail.com")
                .build();
        assertThat(context.getUserId()).hasValue("test@mail.com");
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

        assertThat(context.getUserId()).hasValue("test@mail.com");
        assertThat(context.getSessionId()).hasValue("123");
        assertThat(context.getRemoteAddress()).hasValue("127.0.0.1");
        assertThat(context.getEnvironment()).hasValue("prod");
        assertThat(context.getAppName()).hasValue("myapp");
        assertThat(context.getProperties().get("test")).isEqualTo("me");
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

        assertThat(enhanced.getUserId()).hasValue("test@mail.com");
        assertThat(enhanced.getSessionId()).hasValue("123");
        assertThat(enhanced.getRemoteAddress()).hasValue("127.0.0.1");

        assertThat(enhanced.getEnvironment()).hasValue("stage");
        assertThat(enhanced.getAppName()).hasValue("someApp");
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

        assertThat(enhanced.getUserId()).hasValue("test@mail.com");
        assertThat(enhanced.getSessionId()).hasValue("123");
        assertThat(enhanced.getRemoteAddress()).hasValue("127.0.0.1");
        assertThat(enhanced.getEnvironment()).hasValue("env");
        assertThat(enhanced.getAppName()).hasValue("myApp");
    }

}
