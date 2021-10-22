package no.finn.unleash.variant;

import static java.util.Arrays.asList;
import static no.finn.unleash.Variant.DISABLED_VARIANT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.finn.unleash.ActivationStrategy;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.Variant;
import org.junit.jupiter.api.Test;

public class VariantUtilTest {
    private final ActivationStrategy defaultStrategy =
            new ActivationStrategy("default", Collections.emptyMap());

    @Test
    public void should_return_default_variant_when_toggle_has_no_variants() {
        FeatureToggle toggle = new FeatureToggle("test.variants", true, asList(defaultStrategy));
        UnleashContext context = UnleashContext.builder().build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant).isEqualTo(DISABLED_VARIANT);
    }

    @Test
    public void should_return_variant1() {
        VariantDefinition v1 =
                new VariantDefinition(
                        "a", 33, new Payload("string", "asd"), Collections.emptyList());
        VariantDefinition v2 = new VariantDefinition("b", 33);
        VariantDefinition v3 = new VariantDefinition("c", 34);

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("11").build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v1.getName());
        assertThat(variant.getPayload()).hasValue(v1.getPayload());
        assertThat(variant.isEnabled()).isTrue();
    }

    @Test
    public void should_return_variant2() {
        VariantDefinition v1 =
                new VariantDefinition(
                        "a", 33, new Payload("string", "asd"), Collections.emptyList());
        VariantDefinition v2 = new VariantDefinition("b", 33);
        VariantDefinition v3 = new VariantDefinition("c", 34);

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("163").build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v2.getName());
    }

    @Test
    public void should_return_variant3() {
        VariantDefinition v1 = new VariantDefinition("a", 33);
        VariantDefinition v2 = new VariantDefinition("b", 33);
        VariantDefinition v3 = new VariantDefinition("c", 34);

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("40").build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v3.getName());
    }

    @Test
    public void should_return_variant_override() {
        VariantDefinition v1 = new VariantDefinition("a", 33);
        VariantOverride override = new VariantOverride("userId", asList("11", "12", "123", "44"));
        VariantDefinition v2 = new VariantDefinition("b", 33, null, asList(override));
        VariantDefinition v3 = new VariantDefinition("c", 34);

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("123").build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v2.getName());
    }

    @Test
    public void should_return_variant_override_on_remote_adr() {
        VariantDefinition v1 =
                new VariantDefinition(
                        "a", 33, new Payload("string", "asd"), Collections.emptyList());
        VariantDefinition v2 = new VariantDefinition("b", 33, null, Collections.emptyList());
        VariantOverride override = new VariantOverride("remoteAddress", asList("11.11.11.11"));
        VariantDefinition v3 =
                new VariantDefinition("c", 34, new Payload("string", "blob"), asList(override));

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().remoteAddress("11.11.11.11").build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v3.getName());
        assertThat(variant.getPayload()).hasValue(v3.getPayload());
        assertThat(variant.isEnabled()).isTrue();
    }

    @Test
    public void should_return_variant_override_on_custom_prop() {
        VariantDefinition v1 = new VariantDefinition("a", 33);
        VariantOverride override = new VariantOverride("env", asList("ci", "local", "dev"));
        VariantDefinition v2 = new VariantDefinition("b", 33, null, asList(override));
        VariantDefinition v3 = new VariantDefinition("c", 34);

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context =
                UnleashContext.builder().userId("11").addProperty("env", "dev").build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v2.getName());
    }

    @Test
    public void should_return_variant_override_on_sessionId() {
        String sessionId = "122221";

        VariantDefinition v1 = new VariantDefinition("a", 33);
        VariantOverride override_env = new VariantOverride("env", asList("dev"));
        VariantOverride override_session = new VariantOverride("sessionId", asList(sessionId));
        VariantDefinition v2 =
                new VariantDefinition("b", 33, null, asList(override_env, override_session));
        VariantDefinition v3 = new VariantDefinition("c", 34);

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context =
                UnleashContext.builder()
                        .userId("11")
                        .addProperty("env", "prod")
                        .sessionId(sessionId)
                        .build();

        Variant variant = VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT);

        assertThat(variant.getName()).isEqualTo(v2.getName());
    }

    @Test
    public void should_distribute_variants_according_to_stickiness() {
        VariantDefinition v1 = new VariantDefinition("blue", 1, null, null, "customField");
        VariantDefinition v2 = new VariantDefinition("red", 1, null, null, "customField");
        VariantDefinition v3 = new VariantDefinition("green", 1, null, null, "customField");
        VariantDefinition v4 = new VariantDefinition("yellow", 1, null, null, "customField");
        FeatureToggle variantToggle =
                new FeatureToggle(
                        "toggle-with-variants",
                        true,
                        asList(defaultStrategy),
                        asList(v1, v2, v3, v4));
        UnleashContext context =
                UnleashContext.builder()
                        .userId("11")
                        .addProperty("env", "prod")
                        .sessionId("1222221")
                        .build();
        Map<String, List<Variant>> variantResults =
                IntStream.range(0, 10000)
                        .mapToObj(
                                i ->
                                        VariantUtil.selectVariant(
                                                variantToggle, context, DISABLED_VARIANT))
                        .collect(Collectors.groupingBy(Variant::getName));
        assertThat(variantResults)
                .allSatisfy(
                        (name, variantResult) ->
                                assertThat(variantResult).hasSizeBetween(2400, 2600));
    }

    @Test
    public void should_return_same_variant_when_stickiness_is_set_to_default() {
        VariantDefinition v1 = new VariantDefinition("a", 33, null, null, "default");
        VariantDefinition v2 = new VariantDefinition("b", 33, null, null, "default");
        VariantDefinition v3 = new VariantDefinition("c", 34, null, null, "default");

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("40").build();
        List<Variant> results =
                IntStream.range(0, 500)
                        .mapToObj(i -> VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT))
                        .collect(Collectors.toList());
        assertThat(results)
                .allSatisfy(
                        (Consumer<Variant>)
                                variant -> assertThat(variant.getName()).isEqualTo(v3.getName()));
    }
}
