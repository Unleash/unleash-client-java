package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.repository.FeatureToggleResponse;
import io.getunleash.util.UnleashConfig;

public class AdvancedConstraints {

    public static void main(String[] args) throws InterruptedException {
        UnleashConfig config = UnleashConfig.builder()
                .appName("client-example.advanced.java")
                .customHttpHeader(
                        "Authorization",
                        getOrElse("UNLEASH_API_TOKEN",
                                "default:default.a45fede67f99b17f67312c93e00f448340e7af4ace2b0de2650f5a99"))
                .unleashAPI(getOrElse("UNLEASH_API_URL", "http://localhost:3063/api"))
                .instanceId("java-example")
                .synchronousFetchOnInitialisation(true)
                .sendMetricsInterval(30)
                .subscriber(
                        new UnleashSubscriber() {
                            @Override
                            public void togglesFetched(
                                    FeatureToggleResponse toggleResponse) {
                                System.out.println(toggleResponse);
                                System.out.println(
                                        toggleResponse
                                                .getToggleCollection()
                                                .getFeatures()
                                                .size());
                            }
                        })
                .build();
        Unleash unleash = new DefaultUnleash(config);
        while (true) {
            Thread.sleep(2000);
            UnleashContext context = UnleashContext.builder()
                    .addProperty("semver", "1.5.2")
                    .build();
            System.out.println(
                    unleash.isEnabled("advanced.constraints", context)); // expect this to be true
            UnleashContext smallerSemver = UnleashContext.builder()
                    .addProperty("semver", "1.1.0")
                    .build();
            System.out.println(
                    unleash.isEnabled("advanced.constraints", smallerSemver)); // expect this to be false
        }
    }

    public static String getOrElse(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
}
