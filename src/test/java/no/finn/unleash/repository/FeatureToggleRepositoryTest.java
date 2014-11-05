package no.finn.unleash.repository;

import no.finn.unleash.Toggle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FeatureToggleRepositoryTest {
    private ToggleRepository toggleRepository;
    private String oldTmpDir = System.getProperty("java.io.tmpdir");

    @Before
    public void setup() throws URISyntaxException {
        System.setProperty("java.io.tmpdir", getTmpDir());
        toggleRepository = new FeatureToggleRepository(URI.create("http://trigger.file/fallback"));
    }

    @After
    public void tearDown() {
        System.setProperty("java.io.tmpdir", oldTmpDir);

    }

    @Test
    public void parseInjectedUnleashRepo() throws IOException, URISyntaxException {
        Toggle presentFeature = toggleRepository.getToggle("presentFeature");
        assertNotNull("could not find featureDefaultEnabled", presentFeature);
    }

    @Test
    public void readEnabledToggleFromRepository(){
        Toggle enabledFeature = toggleRepository.getToggle("enabledFeature");
        assertTrue(enabledFeature.isEnabled());
    }


    @Test
    public void readDisabledToggleFromRepository(){
        Toggle disabledFeature = toggleRepository.getToggle("disabledFeature");
        assertFalse("should be disabled", disabledFeature.isEnabled());
    }

    private String getTmpDir() throws URISyntaxException {
        URL resourceUrl = getClass().getResource("/unleash-repo.json");
        Path resourcePath = Paths.get(resourceUrl.toURI());
        return  resourcePath.toString().split("/unleash-repo.json")[0];
    }


}