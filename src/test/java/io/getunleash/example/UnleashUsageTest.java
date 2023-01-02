package io.getunleash.example;

import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UnleashUsageTest {

    @Test
    public void wire() {
        UnleashConfig config =
                new UnleashConfig.Builder()
                        .appName("test")
                        .instanceId("my-hostname:6517")
                        .synchronousFetchOnInitialisation(true)
                        .unleashAPI("http://localhost:4242/api")
                        .build();

        Unleash unleash = new DefaultUnleash(config, new CustomStrategy());

        assertFalse(unleash.isEnabled("myFeature"));
    }
}
