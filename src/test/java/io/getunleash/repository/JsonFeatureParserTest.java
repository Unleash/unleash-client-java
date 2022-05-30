package io.getunleash.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.getunleash.FeatureToggle;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.io.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonFeatureParserTest {
    FeatureRepository repository;

    @BeforeEach
    void setUp() {
        UnleashConfig defaultConfig =
                new UnleashConfig.Builder()
                        .appName("test")
                        .unleashAPI("http://localhost:4242/api/")
                        .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                        .fetchTogglesInterval(200L)
                        .synchronousFetchOnInitialisation(false)
                        .build();

        this.repository = mock(FeatureRepository.class);
    }

    @Test
    public void should_deserialize_correctly() throws IOException {
        Reader content = getFileReader("/features-v2-with-segments.json");
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(content);

        assertThat(featureCollection.getToggleCollection().getFeatures()).hasSize(5);
        assertThat(featureCollection.getToggleCollection().getToggle("featureX").isEnabled())
                .isTrue();
    }

    @Test
    public void should_deserialize_with_one_strategy() throws IOException {
        Reader content = getFileReader("/features-v2-with-segments.json");
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(content);
        FeatureToggle featureY = featureCollection.getToggleCollection().getToggle("featureY");

        assertThat(featureY.getStrategies()).hasSize(1);
        assertThat(featureY.getStrategies().get(0).getName()).isEqualTo("baz");
        assertThat(featureY.getStrategies().get(0).getParameters().get("foo")).isEqualTo("bar");
    }

    @Test
    public void should_deserialize_with_multiple_strategies() throws IOException {
        Reader content = getFileReader("/features-v2-with-segments.json");
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(content);
        FeatureToggle feature = featureCollection.getToggleCollection().getToggle("featureZ");

        assertThat(feature.getStrategies()).hasSize(2);
        assertThat(feature.getStrategies().get(1).getName()).isEqualTo("hola");
        assertThat(feature.getStrategies().get(1).getParameters().get("name")).isEqualTo("val");
    }

    @Test
    public void should_throw() throws IOException {
        Reader content = getFileReader("/empty.json");
        assertThrows(IllegalStateException.class, () -> JsonFeatureParser.fromJson(content));
    }

    @Test
    public void should_throw_on_mission_features() throws IOException {
        Reader content = getFileReader("/empty-v1.json");
        assertThrows(IllegalStateException.class, () -> JsonFeatureParser.fromJson(content));
    }

    @Test
    public void should_deserialize_empty_litst_of_toggles() throws IOException {
        Reader content = getFileReader("/features-v2-empty.json");
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(content);

        assertThat(featureCollection.getToggleCollection().getFeatures()).hasSize(0);
    }

    @Test
    public void should_deserialize_list_of_toggles_with_variants() throws IOException {
        Reader content = getFileReader("/features-v2-with-segments.json");
        FeatureCollection featureCollection = JsonFeatureParser.fromJson(content);
        ToggleCollection toggleCollection = featureCollection.getToggleCollection();

        assertThat(toggleCollection.getFeatures()).hasSize(5);
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
