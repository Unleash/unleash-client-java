package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.util.UnleashConfig;

public class AdvancedConstraints {
    public static void main(String[] args) throws InterruptedException {
        UnleashConfig config = UnleashConfig.builder().appName("client-example.advanced")
            .customHttpHeader("Authorization", "*:development.afaa5c22450312a6e727af54a163cf7aae0115d9ef83deb175b9d311")
            .unleashAPI("http://localhost:4242/api").instanceId("example")
            .build();
        Unleash unleash = new DefaultUnleash(config);
        Thread.sleep(2000);
        UnleashContext context = UnleashContext.builder().addProperty("semver", "1.5.2").build();
        System.out.println(unleash.isEnabled("advanced.constraints", context)); // expect this to be true
        UnleashContext smallerSemver = UnleashContext.builder().addProperty("semver", "1.1.0").build();
        System.out.println(unleash.isEnabled("advanced.constraints", smallerSemver)); // expect this to be false
    }
}
