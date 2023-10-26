package io.getunleash.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.getunleash.variant.VariantUtil;
import java.util.UUID;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;

public class StrategyUtilsTest {

    @Test
    public void normalized_values_are_the_same_across_node_java_and_go_clients() {
        assertEquals(73, StrategyUtils.getNormalizedNumber("123", "gr1", 0));
        assertEquals(25, StrategyUtils.getNormalizedNumber("999", "groupX", 0));
    }

    @Test
    public void normalized_values_with_variant_seed_are_the_same_across_node_java() {
        assertThat(
                        StrategyUtils.getNormalizedNumber(
                                "123", "gr1", VariantUtil.VARIANT_NORMALIZATION_SEED))
                .isEqualTo(96);
        assertThat(
                        StrategyUtils.getNormalizedNumber(
                                "999", "groupX", VariantUtil.VARIANT_NORMALIZATION_SEED))
                .isEqualTo(60);
    }

    @Test
    public void
            selecting_ten_percent_of_users_and_then_finding_variants_should_still_have_variants_evenly_distributed() {
        int ones = 0, twos = 0, threes = 0, loopSize = 500000, selectionSize = 0;
        for (int i = 0; i < loopSize; i++) {
            String id = UUID.randomUUID().toString();
            int featureRollout =
                    StrategyUtils.getNormalizedNumber(id, "feature.name.that.is.quite.long", 0);
            if (featureRollout < 11) {
                int variantGroup =
                        StrategyUtils.getNormalizedNumber(
                                id,
                                "feature.name.that.is.quite.long",
                                1000,
                                VariantUtil.VARIANT_NORMALIZATION_SEED);
                if (variantGroup <= 333) {
                    ones++;
                } else if (variantGroup <= 666) {
                    twos++;
                } else if (variantGroup <= 1000) {
                    threes++;
                }
                selectionSize++;
            }
        }
        assertThat(ones / (double) (selectionSize)).isCloseTo(0.33, Offset.offset(0.01));
        assertThat(twos / (double) (selectionSize)).isCloseTo(0.33, Offset.offset(0.01));
        assertThat(threes / (double) (selectionSize)).isCloseTo(0.33, Offset.offset(0.01));
    }
}
