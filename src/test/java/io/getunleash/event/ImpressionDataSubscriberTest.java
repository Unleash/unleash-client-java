package io.getunleash.event;

import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;

public class ImpressionDataSubscriberTest {

    // private ImpressionTestSubscriber testSubscriber = new ImpressionTestSubscriber();

    private UnleashConfig unleashConfig;

    // private UnleashEngineStateHandler stateHandler;
    private Unleash unleash;

    // @BeforeEach
    // void setup() {
    //     unleashConfig =
    //             new UnleashConfig.Builder()
    //                     .appName(SubscriberTest.class.getSimpleName())
    //                     .instanceId(SubscriberTest.class.getSimpleName())
    //                     .unleashAPI("http://localhost:4242/api")
    //                     .subscriber(testSubscriber)
    //                     .scheduledExecutor(new SynchronousTestExecutor())
    //                     .build();
    //     unleash = new DefaultUnleash(unleashConfig);
    //     // stateHandler = new UnleashEngineStateHandler((DefaultUnleash) unleash);
    // }

    // @Test
    // public void noEventsIfImpressionDataIsNotEnabled() {
    //     String featureWithoutImpressionDataEnabled = "feature.with.no.impressionData";
    //     // stateHandler.setState(
    //     // new FeatureToggle(featureWithoutImpressionDataEnabled, true, new
    //     // ArrayList<>()));
    //     unleash.isEnabled(featureWithoutImpressionDataEnabled);
    //     assertThat(testSubscriber.isEnabledImpressions).isEqualTo(0);
    //     assertThat(testSubscriber.variantImpressions).isEqualTo(0);
    // }

    // @Test
    // public void isEnabledEventWhenImpressionDataIsEnabled() {
    //     String featureWithImpressionData = "feature.with.impressionData";
    //     // new UnleashEngineStateHandler((DefaultUnleash) unleash)
    //     // .setState(
    //     // new FeatureToggle(
    //     // featureWithImpressionData,
    //     // true,
    //     // new ArrayList<>(),
    //     // new ArrayList<>(),
    //     // true));
    //     unleash.isEnabled(featureWithImpressionData);
    //     assertThat(testSubscriber.isEnabledImpressions).isEqualTo(1);
    //     assertThat(testSubscriber.variantImpressions).isEqualTo(0);
    // }

    // @Test
    // public void variantEventWhenVariantIsRequested() {
    //     String featureWithImpressionData = "feature.with.impressionData";
    //     VariantDefinition def = new VariantDefinition("blue", 1000, null, null);
    //     List<VariantDefinition> variants = new ArrayList<>();
    //     variants.add(def);
    //     // stateHandler.setState(
    //     // new FeatureToggle(
    //     // featureWithImpressionData, true, new ArrayList<>(), variants, true));
    //     unleash.getVariant(featureWithImpressionData);
    //     assertThat(testSubscriber.isEnabledImpressions).isEqualTo(0);
    //     assertThat(testSubscriber.variantImpressions).isEqualTo(1);
    // }

    // private class ImpressionTestSubscriber implements UnleashSubscriber {
    //     private int variantImpressions;
    //     private int isEnabledImpressions;

    //     @Override
    //     public void impression(ImpressionEvent impressionEvent) {
    //         if (impressionEvent instanceof VariantImpressionEvent) {
    //             variantImpressions++;
    //         } else if (impressionEvent instanceof IsEnabledImpressionEvent) {
    //             isEnabledImpressions++;
    //         }
    //     }
    // }
}
