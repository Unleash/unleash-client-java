package no.finn.unleash.repository;

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