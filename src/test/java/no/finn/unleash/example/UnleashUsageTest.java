package no.finntech.unleash.example;

import no.finntech.unleash.repository.FeatureToggleRepository;
import no.finntech.unleash.repository.ToggleRepository;
import no.finntech.unleash.UnleashImpl;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertFalse;

public class UnleashUsageTest {

    @Test
    public void wire() {
        ToggleRepository repository = new FeatureToggleRepository(URI.create("http://localhost:4242/features"), 1);

        UnleashImpl unleash = new UnleashImpl(repository, new CustomStrategy());

        assertFalse(unleash.isEnabled("myFeature"));
    }
}
