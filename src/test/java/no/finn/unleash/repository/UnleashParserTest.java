package no.finn.unleash.repository;

import org.junit.Test;

import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class UnleashParserTest {

    @Test
    public void should_deserialize_correctly() throws IOException {
        ToggleCollection toggleCollection = UnleashParser.from(readFile("/features.json"));

        assertThat(toggleCollection.getFeatures().size(), is(3));
    }

    @Test
    public void should_deserialize_one_feature_no_params() throws IOException {
        ToggleCollection toggleCollection = UnleashParser.from(readFile("/one_feature.json"));

        assertThat(toggleCollection.getFeatures().size(), is(1));
    }

    @Test
    public void should_deserialize_two_features_no_params() throws IOException {
        ToggleCollection toggleCollection = UnleashParser.from(readFile("/two_features.json"));

        assertThat(toggleCollection.getFeatures().size(), is(2));
    }

    @Test
    public void should_deserialize_one_feature_with_params() throws IOException {
        ToggleCollection toggleCollection = UnleashParser.from(readFile("/one_feature_with_params.json"));

        assertThat(toggleCollection.getFeatures().size(), is(1));
    }

    private Reader readFile(String filename) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(filename);
        InputStreamReader reader = new InputStreamReader(in);
        return new BufferedReader(reader);
    }


}