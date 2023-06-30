package io.getunleash.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.getunleash.DefaultUnleash;
import io.getunleash.FeatureToggle;
import io.getunleash.SynchronousTestExecutor;
import io.getunleash.Unleash;
import io.getunleash.repository.FeatureRepository;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.VariantDefinition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImpressionDataSubscriberTest {

    private ImpressionTestSubscriber testSubscriber = new ImpressionTestSubscriber();

    private FeatureRepository toggleRepository;

    private UnleashConfig unleashConfig;

    private Unleash unleash;

    @BeforeEach
    void setup() {
        unleashConfig =
                new UnleashConfig.Builder()
                        .appName(SubscriberTest.class.getSimpleName())
                        .instanceId(SubscriberTest.class.getSimpleName())
                        .synchronousFetchOnInitialisation(true)
                        .unleashAPI("http://localhost:4242/api")
                        .subscriber(testSubscriber)
                        .scheduledExecutor(new SynchronousTestExecutor())
                        .build();
        toggleRepository = mock(FeatureRepository.class);
        unleash = new DefaultUnleash(unleashConfig, toggleRepository);
    }

    @Test
    public void noEventsIfImpressionDataIsNotEnabled() {
        String featureWithoutImpressionDataEnabled = "feature.with.no.impressionData";
        when(toggleRepository.getToggle(featureWithoutImpressionDataEnabled))
                .thenReturn(
                        new FeatureToggle(
                                featureWithoutImpressionDataEnabled, true, new ArrayList<>()));
        unleash.isEnabled(featureWithoutImpressionDataEnabled);
        assertThat(testSubscriber.isEnabledImpressions).isEqualTo(0);
        assertThat(testSubscriber.variantImpressions).isEqualTo(0);
    }

    @Test
    public void isEnabledEventWhenImpressionDataIsEnabled() {
        String featureWithImpressionData = "feature.with.impressionData";
        when(toggleRepository.getToggle(featureWithImpressionData))
                .thenReturn(
                        new FeatureToggle(
                                featureWithImpressionData,
                                true,
                                new ArrayList<>(),
                                new ArrayList<>(),
                                true));
        unleash.isEnabled(featureWithImpressionData);
        assertThat(testSubscriber.isEnabledImpressions).isEqualTo(1);
        assertThat(testSubscriber.variantImpressions).isEqualTo(0);
    }

    @Test
    public void variantEventWhenVariantIsRequested() {
        String featureWithImpressionData = "feature.with.impressionData";
        VariantDefinition def = new VariantDefinition("blue", 1000, null, null);
        List<VariantDefinition> variants = new ArrayList<>();
        variants.add(def);
        when(toggleRepository.getToggle(featureWithImpressionData))
                .thenReturn(
                        new FeatureToggle(
                                featureWithImpressionData,
                                true,
                                new ArrayList<>(),
                                variants,
                                true));
        unleash.getVariant(featureWithImpressionData);
        assertThat(testSubscriber.isEnabledImpressions).isEqualTo(0);
        assertThat(testSubscriber.variantImpressions).isEqualTo(1);
    }

    private class ImpressionTestSubscriber implements UnleashSubscriber {
        private int variantImpressions;
        private int isEnabledImpressions;

        @Override
        public void impression(ImpressionEvent impressionEvent) {
            if (impressionEvent instanceof VariantImpressionEvent) {
                variantImpressions++;
            } else if (impressionEvent instanceof IsEnabledImpressionEvent) {
                isEnabledImpressions++;
            }
        }
    }
}
