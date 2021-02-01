package no.finn.unleash.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.*;
import no.finn.unleash.FeatureToggle;
import org.junit.jupiter.api.Test;

public class JsonFeatureToggleParserTest {

    @Test
    public void should_deserialize_correctly() throws IOException {
        Reader content = getFileReader("/features-v1.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures()).hasSize(3);
        assertThat(toggleCollection.getToggle("featureX").isEnabled()).isTrue();
    }

    @Test
    public void should_deserialize_correctly_version0() throws IOException {
        Reader content = getFileReader("/features-v0.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures()).hasSize(3);
        assertThat(toggleCollection.getToggle("featureX").isEnabled()).isTrue();
    }

    @Test
    public void should_deserialize_with_one_strategy() throws IOException {
        Reader content = getFileReader("/features-v1.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(featureY.getStrategies()).hasSize(1);
        assertThat(featureY.getStrategies().get(0).getName()).isEqualTo("baz");
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo")).isEqualTo("bar");
    }

    @Test
    public void should_deserialize_with_one_strategy_version0() throws IOException {
        Reader content = getFileReader("/features-v0.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(featureY.isEnabled()).isFalse();
        assertThat(featureY.getStrategies()).hasSize(1);
        assertThat(featureY.getStrategies().get(0).getName()).isEqualTo("baz");
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo")).isEqualTo("bar");
    }

    @Test
    public void should_deserialize_with_multiple_strategies() throws IOException {
        Reader content = getFileReader("/features-v1.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle feature = toggleCollection.getToggle("featureZ");

        assertThat(feature.getStrategies()).hasSize(2);
        assertThat(feature.getStrategies().get(1).getName()).isEqualTo("hola");
        assertThat(feature.getStrategies().get(1).getParameters().get("name")).isEqualTo("val");
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

        assertThat(toggleCollection.getFeatures()).hasSize(0);
    }

    @Test
    public void should_deserialize_old_format() throws IOException {
        Reader content = getFileReader("/features-v0.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);
        FeatureToggle featureY = toggleCollection.getToggle("featureY");

        assertThat(toggleCollection.getFeatures()).hasSize(3);
        assertThat(featureY.getStrategies()).hasSize(1);
        assertThat(featureY.getStrategies().get(0).getName()).isEqualTo("baz");
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo")).isEqualTo("bar");
    }

    @Test
    public void should_deserialize_list_of_toggles_with_variants() throws IOException {
        Reader content = getFileReader("/features-v1-with-variants.json");
        ToggleCollection toggleCollection = JsonToggleParser.fromJson(content);

        assertThat(toggleCollection.getFeatures()).hasSize(2);
        assertThat(toggleCollection.getToggle("Test.old").isEnabled()).isTrue();
        assertThat(toggleCollection.getToggle("Test.variants").isEnabled()).isTrue();
        assertThat(toggleCollection.getToggle("Test.variants").getVariants()).isNotNull();
        assertThat(toggleCollection.getToggle("Test.variants").getVariants()).hasSize(2);
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(0).getName())
                .isEqualTo("variant1");
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(1).getName())
                .isEqualTo("variant2");
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(0).getWeight())
                .isEqualTo(50);
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(1).getWeight())
                .isEqualTo(50);
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(0).getPayload())
                .isNull();
        assertThat(toggleCollection.getToggle("Test.variants").getVariants().get(1).getPayload())
                .isNull();
    }

    private Reader getFileReader(String filename) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(filename);
        InputStreamReader reader = new InputStreamReader(in);
        return new BufferedReader(reader);
    }
}
