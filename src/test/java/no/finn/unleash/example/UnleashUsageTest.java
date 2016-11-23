package no.finn.unleash.example;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class UnleashUsageTest {

    @Test
    public void wire() {
        UnleashConfig config = new UnleashConfig.Builder()
                .appName("test")
                .instanceId("my-hostname:6517")
                .unleashAPI("http://localhost:4242")
                .build();

        Unleash unleash = new DefaultUnleash(config, new CustomStrategy());

        assertFalse(unleash.isEnabled("myFeature"));
    }
}
