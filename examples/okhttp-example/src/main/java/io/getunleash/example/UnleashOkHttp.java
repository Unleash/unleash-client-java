package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.UnleashException;
import io.getunleash.event.UnleashReady;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.repository.FeatureToggleResponse;
import io.getunleash.repository.OkHttpFeatureFetcher;
import io.getunleash.util.UnleashConfig;

public class UnleashOkHttp {
    public static void main(String[] args) throws InterruptedException {

        UnleashConfig config = UnleashConfig.builder().appName("client-example.okhttp")
                .customHttpHeader("Authorization",
                        "*:production.ZvzGdauVXYPyevrQVqnt8LSRHKuW")
                .unleashAPI("http://localhost:1500/api").instanceId("okhttp-example")
                .unleashFeatureFetcherFactory(OkHttpFeatureFetcher::new)
                .fetchTogglesInterval(10)
            .subscriber(new UnleashSubscriber() {
                @Override
                public void onReady(UnleashReady unleashReady) {
                    System.out.println("Ready");
                }

                @Override
                public void togglesFetched(FeatureToggleResponse toggleResponse) {
                    System.out.println("Fetched toggles. "  + toggleResponse);
                }

                @Override
                public void onError(UnleashException unleashException) {
                    System.out.println("Failed " + unleashException);
                }
            })
                .synchronousFetchOnInitialisation(true)
                .build();
        Unleash unleash = new DefaultUnleash(config);
        unleash.more().getFeatureToggleNames().forEach(t -> System.out.println(t));
        while (true) {
            Thread.sleep(5000);
            System.out.println(unleash.isEnabled("my.feature",
                    UnleashContext.builder().addProperty("email", "test@getunleash.ai").build()));
            System.out.println(unleash.getVariant("my.feature"));
        }
    }
}
