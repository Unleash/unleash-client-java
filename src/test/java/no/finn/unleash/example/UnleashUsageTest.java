package no.finn.unleash.example;

import no.finn.unleash.Unleash;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.DefaultUnleash;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertFalse;

public class UnleashUsageTest {

    @Test
    public void wire() {
        Unleash unleash = new DefaultUnleash(URI.create("http://localhost:4242/features"), new CustomStrategy());

        assertFalse(unleash.isEnabled("myFeature"));
    }
}
