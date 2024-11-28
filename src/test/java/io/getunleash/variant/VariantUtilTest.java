package io.getunleash.variant;

import static io.getunleash.Variant.DISABLED_VARIANT;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.getunleash.ActivationStrategy;
import io.getunleash.FeatureToggle;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VariantUtilTest {

    private ActivationStrategy defaultStrategy;

    @BeforeEach
    void setUp() {
        UnleashConfig defaultConfig =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .fetchTogglesInterval(200L)
                        .synchronousFetchOnInitialisation(true)
                        .build();

        defaultStrategy = new ActivationStrategy("default", Collections.emptyMap());
    }

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

        UnleashContext context = UnleashContext.builder().userId("80").build();

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

        UnleashContext context = UnleashContext.builder().userId("163").build();

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
                                assertThat(variantResult).hasSizeBetween(2300, 2700));
    }

    @Test
    public void should_return_same_variant_when_stickiness_is_set_to_default() {
        VariantDefinition v1 = new VariantDefinition("a", 33, null, null, "default");
        VariantDefinition v2 = new VariantDefinition("b", 33, null, null, "default");
        VariantDefinition v3 = new VariantDefinition("c", 34, null, null, "default");

        FeatureToggle toggle =
                new FeatureToggle(
                        "test.variants", true, asList(defaultStrategy), asList(v1, v2, v3));

        UnleashContext context = UnleashContext.builder().userId("163;").build();
        List<Variant> results =
                IntStream.range(0, 500)
                        .mapToObj(i -> VariantUtil.selectVariant(toggle, context, DISABLED_VARIANT))
                        .collect(Collectors.toList());
        assertThat(results)
                .allSatisfy(
                        (Consumer<Variant>)
                                variant -> assertThat(variant.getName()).isEqualTo(v3.getName()));
    }

    @Test
    public void custom_stickiness_variants() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("rollout", "100");
        parameters.put("stickiness", "customField");
        parameters.put("groupId", "Feature.flexible.rollout.custom.stickiness_100");
        ActivationStrategy flexibleRollout = new ActivationStrategy("flexibleRollout", parameters);
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(
                new VariantDefinition(
                        "blue",
                        25,
                        new Payload("string", "val1"),
                        Collections.emptyList(),
                        "customField"));
        variants.add(
                new VariantDefinition(
                        "red",
                        25,
                        new Payload("string", "val1"),
                        Collections.emptyList(),
                        "customField"));
        variants.add(
                new VariantDefinition(
                        "green",
                        25,
                        new Payload("string", "val1"),
                        Collections.emptyList(),
                        "customField"));
        variants.add(
                new VariantDefinition(
                        "yellow",
                        25,
                        new Payload("string", "val1"),
                        Collections.emptyList(),
                        "customField"));
        FeatureToggle toggle =
                new FeatureToggle(
                        "Feature.flexible.rollout.custom.stickiness_100",
                        true,
                        asList(flexibleRollout),
                        variants);
        Variant variantCustom616 =
                VariantUtil.selectVariant(
                        toggle,
                        UnleashContext.builder().addProperty("customField", "616").build(),
                        DISABLED_VARIANT);
        assertThat(variantCustom616.getName()).isEqualTo("blue");
        Variant variantCustom503 =
                VariantUtil.selectVariant(
                        toggle,
                        UnleashContext.builder().addProperty("customField", "503").build(),
                        DISABLED_VARIANT);
        assertThat(variantCustom503.getName()).isEqualTo("red");
        Variant variantCustom438 =
                VariantUtil.selectVariant(
                        toggle,
                        UnleashContext.builder().addProperty("customField", "438").build(),
                        DISABLED_VARIANT);
        assertThat(variantCustom438.getName()).isEqualTo("green");
        Variant variantCustom44 =
                VariantUtil.selectVariant(
                        toggle,
                        UnleashContext.builder().addProperty("customField", "44").build(),
                        DISABLED_VARIANT);
        assertThat(variantCustom44.getName()).isEqualTo("yellow");
    }

    @Test
    public void feature_variants_variant_b_client_spec_tests() {
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(
                new VariantDefinition(
                        "variant1", 1, new Payload("string", "val1"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant2", 1, new Payload("string", "val2"), Collections.emptyList()));
        FeatureToggle toggle =
                new FeatureToggle("Feature.Variants.B", true, Collections.emptyList(), variants);
        Variant variantUser2 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("2").build(), DISABLED_VARIANT);
        assertThat(variantUser2.getName()).isEqualTo("variant2");
        Variant variantUser0 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("0").build(), DISABLED_VARIANT);
        assertThat(variantUser0.getName()).isEqualTo("variant1");
    }

    @Test
    public void feature_variants_variant_c_client_spec_tests() {
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(
                new VariantDefinition(
                        "variant1", 33, new Payload("string", "val1"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant2", 33, new Payload("string", "val1"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant3", 33, new Payload("string", "val1"), Collections.emptyList()));
        FeatureToggle toggle =
                new FeatureToggle("Feature.Variants.C", true, Collections.emptyList(), variants);
        Variant variantUser232 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("232").build(), DISABLED_VARIANT);
        assertThat(variantUser232.getName()).isEqualTo("variant1");
        Variant variantUser607 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("607").build(), DISABLED_VARIANT);
        assertThat(variantUser607.getName()).isEqualTo("variant2");
        Variant variantUser656 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("656").build(), DISABLED_VARIANT);
        assertThat(variantUser656.getName()).isEqualTo("variant3");
    }

    @Test
    public void feature_variants_variant_d_client_spec_tests() {
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(
                new VariantDefinition(
                        "variant1", 1, new Payload("string", "val1"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant2", 49, new Payload("string", "val2"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant3", 50, new Payload("string", "val3"), Collections.emptyList()));
        FeatureToggle toggle =
                new FeatureToggle("Feature.Variants.D", true, Collections.emptyList(), variants);
        Variant variantUser712 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("712").build(), DISABLED_VARIANT);
        assertThat(variantUser712.getName()).isEqualTo("variant1");
        Variant variantUser525 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("525").build(), DISABLED_VARIANT);
        assertThat(variantUser525.getName()).isEqualTo("variant2");
        Variant variantUser537 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("537").build(), DISABLED_VARIANT);
        assertThat(variantUser537.getName()).isEqualTo("variant3");
    }

    @Test
    public void feature_variants_variant_d_client_spec_tests_with_deprecated_seed() {
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(
                new VariantDefinition(
                        "variant1", 1, new Payload("string", "val1"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant2", 49, new Payload("string", "val2"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant3", 50, new Payload("string", "val3"), Collections.emptyList()));
        FeatureToggle toggle =
                new FeatureToggle("Feature.Variants.D", true, Collections.emptyList(), variants);
        Variant variantUser712 =
                VariantUtil.selectDeprecatedVariantHashingAlgo(
                        toggle, UnleashContext.builder().userId("712").build(), DISABLED_VARIANT);
        assertThat(variantUser712.getName()).isEqualTo("variant3");
        Variant variantUser525 =
                VariantUtil.selectDeprecatedVariantHashingAlgo(
                        toggle, UnleashContext.builder().userId("525").build(), DISABLED_VARIANT);
        assertThat(variantUser525.getName()).isEqualTo("variant3");
        Variant variantUser537 =
                VariantUtil.selectDeprecatedVariantHashingAlgo(
                        toggle, UnleashContext.builder().userId("537").build(), DISABLED_VARIANT);
        assertThat(variantUser537.getName()).isEqualTo("variant2");
    }

    @Test
    public void feature_variants_variant_d_with_override_client_spec_tests() {
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(
                new VariantDefinition(
                        "variant1",
                        33,
                        new Payload("string", "val1"),
                        Arrays.asList(new VariantOverride("userId", asList("132", "61")))));
        variants.add(
                new VariantDefinition(
                        "variant2", 33, new Payload("string", "val2"), Collections.emptyList()));
        variants.add(
                new VariantDefinition(
                        "variant3", 34, new Payload("string", "val3"), Collections.emptyList()));
        FeatureToggle toggle =
                new FeatureToggle(
                        "Feature.Variants.override.D", true, Collections.emptyList(), variants);
        Variant variantUser10 =
                VariantUtil.selectVariant(
                        toggle, UnleashContext.builder().userId("10").build(), DISABLED_VARIANT);
        assertThat(variantUser10.getName()).isEqualTo("variant2");
    }
}
