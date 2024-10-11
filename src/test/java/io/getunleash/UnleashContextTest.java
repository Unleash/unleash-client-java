package io.getunleash;

import io.getunleash.util.UnleashConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

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
        UnleashContext context = UnleashContext.builder().userId("test@mail.com").build();
        assertThat(context.getUserId()).hasValue("test@mail.com");
    }

    @Test
    public void should_get_context_with_fields_set() {
        UnleashContext context =
                UnleashContext.builder()
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
        assertThat(context.getProperties()).containsExactly(entry("test", "me"));
    }

    @Test
    public void should_apply_context_fields() {
        UnleashContext context =
                UnleashContext.builder()
                        .userId("test@mail.com")
                        .sessionId("123")
                        .remoteAddress("127.0.0.1")
                        .addProperty("test", "me")
                        .build();

        UnleashConfig config =
                UnleashConfig.builder()
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
    public void should_not_override_static_context_fields() {
        UnleashContext context =
                UnleashContext.builder()
                        .userId("test@mail.com")
                        .sessionId("123")
                        .remoteAddress("127.0.0.1")
                        .environment("env")
                        .appName("myApp")
                        .addProperty("test", "me")
                        .build();

        UnleashConfig config =
                UnleashConfig.builder()
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

    @Nested
    class BuilderTest {

        @Test
        void should_set_special_properties() {
            final UnleashContext context = UnleashContext.builder()
                .addProperty("userId", "test@mail.com")
                .addProperty("sessionId", "123")
                .addProperty("remoteAddress", "127.0.0.1")
                .addProperty("environment", "env")
                .addProperty("appName", "myApp")
                .build();

            assertThat(context.getUserId()).contains("test@mail.com");
            assertThat(context.getSessionId()).contains("123");
            assertThat(context.getRemoteAddress()).contains("127.0.0.1");
            assertThat(context.getEnvironment()).contains("env");
            assertThat(context.getAppName()).contains("myApp");
        }

        @Test
        void should_set_non_special_properties() {
            final UnleashContext context = UnleashContext.builder()
                .addProperty("foo", "bar")
                .build();

            assertThat(context.getProperties()).containsExactly(entry("foo", "bar"));
        }

    }

}
