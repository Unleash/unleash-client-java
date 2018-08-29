package no.finn.unleash.example;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.TestUtil;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class UnleashUsageTest {

    @Test
    public void wire() {
        TestUtil.setLogLevel(Level.ERROR); //Mute warn messages.
        UnleashConfig config = new UnleashConfig.Builder()
                .appName("test")
                .instanceId("my-hostname:6517")
                .synchronousFetchOnInitialisation(true)
                .unleashAPI("http://localhost:4242/api")
                .build();

        Unleash unleash = new DefaultUnleash(config, new CustomStrategy());

        assertFalse(unleash.isEnabled("myFeature"));
    }
}
