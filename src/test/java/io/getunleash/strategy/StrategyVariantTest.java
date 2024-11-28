package io.getunleash.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import io.getunleash.FeatureEvaluationResult;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.getunleash.variant.Payload;
import io.getunleash.variant.VariantDefinition;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

public class StrategyVariantTest {

    @Test
    public void should_inherit_stickiness_from_the_strategy_first_variant() {
        HashMap<String, String> params = new HashMap<>();
        params.put("rollout", "100");
        params.put("stickiness", "clientId");
        params.put("groupId", "a");
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        VariantDefinition varA =
                new VariantDefinition(
                        "variantNameA",
                        1,
                        new Payload("string", "variantValueA"),
                        Collections.emptyList());
        VariantDefinition varB =
                new VariantDefinition(
                        "variantNameB",
                        1,
                        new Payload("string", "variantValueB"),
                        Collections.emptyList());

        UnleashContext context = UnleashContext.builder().addProperty("clientId", "1").build();
        FeatureEvaluationResult result =
                strategy.getResult(
                        params, context, Collections.emptyList(), Arrays.asList(varA, varB));
        Variant selectedVariant = result.getVariant();
        assert selectedVariant != null;
        assertThat(selectedVariant.getName()).isEqualTo("variantNameA");
    }

    @Test
    public void should_inherit_stickiness_from_the_strategy_second_variant() {
        HashMap<String, String> params = new HashMap<>();
        params.put("rollout", "100");
        params.put("stickiness", "clientId");
        params.put("groupId", "a");
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        VariantDefinition varA =
                new VariantDefinition(
                        "variantNameA",
                        1,
                        new Payload("string", "variantValueA"),
                        Collections.emptyList());
        VariantDefinition varB =
                new VariantDefinition(
                        "variantNameB",
                        1,
                        new Payload("string", "variantValueB"),
                        Collections.emptyList());

        UnleashContext context = UnleashContext.builder().addProperty("clientId", "2").build();
        FeatureEvaluationResult result =
                strategy.getResult(
                        params, context, Collections.emptyList(), Arrays.asList(varA, varB));
        Variant selectedVariant = result.getVariant();
        assert selectedVariant != null;
        assertThat(selectedVariant.getName()).isEqualTo("variantNameB");
    }

    @Test
    public void multiple_variants_should_choose_first_variant() {
        HashMap<String, String> params = new HashMap<>();
        params.put("rollout", "100");
        params.put("groupId", "a");
        params.put("stickiness", "default");
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        VariantDefinition varA =
                new VariantDefinition(
                        "variantNameA",
                        1,
                        new Payload("string", "variantValueA"),
                        Collections.emptyList());
        VariantDefinition varB =
                new VariantDefinition(
                        "variantNameB",
                        1,
                        new Payload("string", "variantValueB"),
                        Collections.emptyList());
        UnleashContext context = UnleashContext.builder().userId("5").build();
        FeatureEvaluationResult result =
                strategy.getResult(
                        params, context, Collections.emptyList(), Arrays.asList(varA, varB));
        Variant selectedVariant = result.getVariant();
        assertThat(selectedVariant.getName()).isEqualTo("variantNameA");
    }

    @Test
    public void multiple_variants_should_choose_second_variant() {
        HashMap<String, String> params = new HashMap<>();
        params.put("rollout", "100");
        params.put("groupId", "a");
        params.put("stickiness", "default");
        FlexibleRolloutStrategy strategy = new FlexibleRolloutStrategy();
        VariantDefinition varA =
                new VariantDefinition(
                        "variantNameA",
                        1,
                        new Payload("string", "variantValueA"),
                        Collections.emptyList());
        VariantDefinition varB =
                new VariantDefinition(
                        "variantNameB",
                        1,
                        new Payload("string", "variantValueB"),
                        Collections.emptyList());
        UnleashContext context = UnleashContext.builder().userId("0").build();
        FeatureEvaluationResult result =
                strategy.getResult(
                        params, context, Collections.emptyList(), Arrays.asList(varA, varB));
        Variant selectedVariant = result.getVariant();
        assertThat(selectedVariant.getName()).isEqualTo("variantNameB");
    }
}
