package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;
import org.junit.Test;


import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JsonFeatureToggleParserTest {

    @Test
    public void should_deserialize_correctly() throws IOException {
        Reader content = getFileReader("/features.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures().size(), is(3));
    }

    @Test
    public void should_deserialize_with_one_strategy() throws IOException {
        Reader content = getFileReader("/features.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(featureY.getStrategies().size(), is(1));
        assertThat(featureY.getStrategies().get(0).getName(), is("baz"));
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo"), is("bar"));
    }

    @Test
    public void should_deserialize_with_multiple_strategies() throws IOException {
        Reader content = getFileReader("/features.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle feature = toggleCollection.getToggle("featureZ");

        assertThat(feature.getStrategies().size(), is(2));
        assertThat(feature.getStrategies().get(1).getName(), is("hola"));
        assertThat(feature.getStrategies().get(1).getParameters().get("name"), is("val"));
    }


    @Test
    public void shouldThrow() throws IOException {
        Reader content = getFileReader("/empty.json");
        try {
            JsonToggleParser.fromJson(content);
        } catch (IllegalStateException e) {
            assertTrue("Expected IllegalStateException", e instanceof IllegalStateException);
        }
    }

    private Reader getFileReader(String filename) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(filename);
        InputStreamReader reader = new InputStreamReader(in);
        return new BufferedReader(reader);
    }
}