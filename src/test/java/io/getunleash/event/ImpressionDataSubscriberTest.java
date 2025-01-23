package io.getunleash.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.getunleash.DefaultUnleash;
import io.getunleash.EngineProxy;
import io.getunleash.SynchronousTestExecutor;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.util.UnleashConfig;
import io.getunleash.variant.Variant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ImpressionDataSubscriberTest {

    private ImpressionTestSubscriber testSubscriber = new ImpressionTestSubscriber();

    private UnleashConfig unleashConfig;

    @BeforeEach
    void setup() {
        unleashConfig =
                new UnleashConfig.Builder()
                        .appName(SubscriberTest.class.getSimpleName())
                        .instanceId(SubscriberTest.class.getSimpleName())
                        .unleashAPI("http://localhost:4242/api")
                        .subscriber(testSubscriber)
                        .scheduledExecutor(new SynchronousTestExecutor())
                        .build();
    }

    @Test
    public void noEventsIfImpressionDataIsNotEnabled() {
        String featureWithoutImpressionDataEnabled = "feature.with.no.impressionData";
        EngineProxy repo = Mockito.mock(EngineProxy.class);
        when(repo.isEnabled(any(String.class), any(UnleashContext.class))).thenReturn(true);
        when(repo.shouldEmitImpressionEvent(featureWithoutImpressionDataEnabled)).thenReturn(false);
        Unleash unleash = new DefaultUnleash(unleashConfig, repo);

        unleash.isEnabled(featureWithoutImpressionDataEnabled);
        assertThat(testSubscriber.isEnabledImpressions).isEqualTo(0);
        assertThat(testSubscriber.variantImpressions).isEqualTo(0);
    }

    @Test
    public void isEnabledEventWhenImpressionDataIsEnabled() {
        String featureWithImpressionData = "feature.with.impressionData";
        EngineProxy repo = Mockito.mock(EngineProxy.class);
        when(repo.isEnabled(any(String.class), any(UnleashContext.class))).thenReturn(true);
        when(repo.shouldEmitImpressionEvent(featureWithImpressionData)).thenReturn(true);
        Unleash unleash = new DefaultUnleash(unleashConfig, repo);

        unleash.isEnabled(featureWithImpressionData);
        assertThat(testSubscriber.isEnabledImpressions).isEqualTo(1);
        assertThat(testSubscriber.variantImpressions).isEqualTo(0);
    }

    @Test
    public void variantEventWhenVariantIsRequested() {
        String featureWithImpressionData = "feature.with.impressionData";
        EngineProxy repo = Mockito.mock(EngineProxy.class);
        when(repo.shouldEmitImpressionEvent(featureWithImpressionData)).thenReturn(true);
        when(repo.getVariant(any(String.class), any(UnleashContext.class), any(Variant.class)))
                .thenReturn(new Variant("variant1", null, true, true));
        Unleash unleash = new DefaultUnleash(unleashConfig, repo);

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
