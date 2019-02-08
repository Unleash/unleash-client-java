package no.finn.unleash.repository;

import no.finn.unleash.FeatureToggle;
import org.junit.jupiter.api.Test;


import java.io.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.not;

public class JsonFeatureToggleParserTest {

    @Test
    public void should_deserialize_correctly() throws IOException {
        Reader content = getFileReader("/features-v1.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures().size(), is(3));
        assertThat(toggleCollection.getToggle("featureX").isEnabled(), is(true));
    }

    @Test
    public void should_deserialize_correctly_version0() throws IOException {
        Reader content = getFileReader("/features-v0.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures().size(), is(3));
        assertThat(toggleCollection.getToggle("featureX").isEnabled(), is(true));
    }

    @Test
    public void should_deserialize_with_one_strategy() throws IOException {
        Reader content = getFileReader("/features-v1.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(featureY.getStrategies().size(), is(1));
        assertThat(featureY.getStrategies().get(0).getName(), is("baz"));
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo"), is("bar"));
    }

    @Test
    public void should_deserialize_with_one_strategy_version0() throws IOException {
        Reader content = getFileReader("/features-v0.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(featureY.isEnabled(), is(false));
        assertThat(featureY.getStrategies().size(), is(1));
        assertThat(featureY.getStrategies().get(0).getName(), is("baz"));
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo"), is("bar"));
    }

    @Test
    public void should_deserialize_with_multiple_strategies() throws IOException {
        Reader content = getFileReader("/features-v1.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle feature = toggleCollection.getToggle("featureZ");

        assertThat(feature.getStrategies().size(), is(2));
        assertThat(feature.getStrategies().get(1).getName(), is("hola"));
        assertThat(feature.getStrategies().get(1).getParameters().get("name"), is("val"));
    }

    @Test
    public void should_throw() throws IOException {
        Reader content = getFileReader("/empty.json");
        assertThrows(IllegalStateException.class, () -> JsonToggleParser.fromJson(content));
    }

    @Test
    public void should_throw_on_mission_features() throws IOException {
        Reader content = getFileReader("/empty-v1.json");
        assertThrows(IllegalStateException.class, () -> JsonToggleParser.fromJson(content));
    }

    @Test
    public void should_deserialize_empty_litst_of_toggles() throws IOException {
        Reader content = getFileReader("/features-v1-empty.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures().size(), is(0));
    }

    @Test
    public void should_deserialize_old_format() throws IOException {
        Reader content = getFileReader("/features-v0.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(toggleCollection.getFeatures().size(), is(3));
        assertThat(featureY.getStrategies().size(), is(1));
        assertThat(featureY.getStrategies().get(0).getName(), is("baz"));
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo"), is("bar"));
    }

    @Test
    public void should_deserialize_list_of_toggles_with_variants() throws IOException {
        Reader content = getFileReader("/features-v1-with-variants.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures().size(), is(2));
        assertThat(toggleCollection.getToggle("Test.old").isEnabled(), is(true));
        assertThat(toggleCollection.getToggle("Test.variants").isEnabled(), is(true));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants(), is(notNullValue()));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().size(), is(2));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(0).getName(), is("variant1"));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(1).getName(), is("variant2"));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(0).getWeight(), is(50));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(1).getWeight(), is(50));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(0).getPayload(), is(nullValue()));
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(1).getPayload(), is(nullValue()));
    }

    private Reader getFileReader(String filename) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(filename);
        InputStreamReader reader = new InputStreamReader(in);
        return new BufferedReader(reader);
    }
}