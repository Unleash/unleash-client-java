package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.repository.FeatureToggleResponse;
import io.getunleash.util.UnleashConfig;

public class AdvancedConstraints {
    public static void main(String[] args) throws InterruptedException {
        UnleashConfig config = UnleashConfig.builder().appName("client-example.advanced")
                .customHttpHeader("Authorization",
                        "*:production.ZvzGdauVXYPyevrQVqnt8LSRHKuW")
                .unleashAPI("http://localhost:1500/api").instanceId("example")
                .synchronousFetchOnInitialisation(true)
            .subscriber(new UnleashSubscriber() {
                @Override
                public void togglesFetched(FeatureToggleResponse toggleResponse) {
                    System.out.println(toggleResponse);
                    System.out.println(toggleResponse.getToggleCollection().getFeatures().size());
                }
            })
                .build();
        Unleash unleash = new DefaultUnleash(config);
        while (true) {
            Thread.sleep(2000);
            UnleashContext context = UnleashContext.builder().addProperty("semver", "1.5.2").build();
            System.out.println(unleash.isEnabled("advanced.constraints", context)); // expect this to be true
            UnleashContext smallerSemver = UnleashContext.builder().addProperty("semver", "1.1.0").build();
            System.out.println(unleash.isEnabled("advanced.constraints", smallerSemver)); // expect this to be false
        }
    }
}
