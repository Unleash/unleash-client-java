package no.finn.unleash.repository;

import java.net.URI;
import org.junit.Test;

import static org.junit.Assert.assertNull;


public class FeatureFeatureToggleRepositoryTest {
    private ToggleRepository toggleRepository;

    @Test
    public void noBackupFileAndNoRepositoryAvailable(){
        toggleRepository = new FeatureToggleRepository(URI.create("http://localhost:4242/features"));
        assertNull("should be null", toggleRepository.getToggle("unknownFeature"));
    }

}