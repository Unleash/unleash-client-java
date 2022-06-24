package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.repository.OkHttpFeatureFetcher;
import io.getunleash.util.UnleashConfig;

public class UnleashOkHttp {
    public static void main(String[] args) throws InterruptedException {
        UnleashConfig config = UnleashConfig.builder().appName("client-example.okhttp")
            .customHttpHeader("Authorization", "*:development.afaa5c22450312a6e727af54a163cf7aae0115d9ef83deb175b9d311")
            .unleashAPI("http://localhost:4242/api").instanceId("okhttp-example")
            .unleashFeatureFetcherFactory(() -> new OkHttpFeatureFetcher())
            .synchronousFetchOnInitialisation(true)
            .build();
        Unleash unleash = new DefaultUnleash(config);
        while(true) {
            Thread.sleep(100);
            System.out.println(unleash.isEnabled("my.feature", UnleashContext.builder().addProperty("email", "test@getunleash.ai").build()));
            System.out.println(unleash.getVariant("my.feature"));
        }
    }
}
