package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.util.UnleashConfig;

public class AdvancedConstraints {
    public static void main(String[] args) throws InterruptedException {
        UnleashConfig config = UnleashConfig.builder().appName("client-example.advanced")
            .customHttpHeader("Authorization", "default:development.5ad8536b3f4f527355a92a628b4519751804d499ac7155794eb039bf")
            .unleashAPI("https://app.unleash-hosted.com/hosted/api").instanceId("example")
            .build();
        Unleash unleash = new DefaultUnleash(config);
        Thread.sleep(2000);
        UnleashContext context = UnleashContext.builder().addProperty("semver", "1.5.2").build();
        System.out.println(unleash.isEnabled("advanced.constraints", context)); // expect this to be true
        UnleashContext smallerSemver = UnleashContext.builder().addProperty("semver", "1.1.0").build();
        System.out.println(unleash.isEnabled("advanced.constraints", smallerSemver)); // expect this to be false
    }
}
