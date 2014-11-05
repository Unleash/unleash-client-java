package no.finn.unleash.repository;

import no.finn.unleash.Toggle;
import org.junit.Test;


import java.net.URI;

import static org.junit.Assert.*;

public class FeatureToggleRepositoryTest {
    private ToggleRepository toggleRepository;



    @Test
    public void readDisabledToggleFromRepository(){
        toggleRepository = new FeatureToggleRepository(URI.create("http://localhost:4242/features"));
        Toggle unknownFeature = toggleRepository.getToggle("unknownFeature");
        assertFalse("should be disabled", unknownFeature.isEnabled());
    }




}