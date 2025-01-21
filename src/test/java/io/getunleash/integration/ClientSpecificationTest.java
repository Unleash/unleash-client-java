package io.getunleash.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.getunleash.repository.ToggleBootstrapProvider;
import io.getunleash.strategy.constraints.DateParser;
import io.getunleash.util.UnleashConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public class ClientSpecificationTest {

    @TestFactory
    public Stream<DynamicTest> clientSpecification() throws IOException, URISyntaxException {
        Reader content = getFileReader("/client-specification/specifications/index.json");
        List<String> testDefinitions =
                new Gson().fromJson(content, new TypeToken<List<String>>() {}.getType());

        List<DynamicTest> tests = new ArrayList<>();
        for (String name : testDefinitions) {
            tests.addAll(createTests(name));
            tests.addAll(createVariantTests(name));
        }
        return tests.stream();
    }

    private List<DynamicTest> createTests(String fileName) throws IOException, URISyntaxException {
        TestDefinition testDefinition = getTestDefinition(fileName);

        Unleash unleash = setupUnleash(testDefinition);

        // Create all test cases in testDefinition.
        return testDefinition.getTests().stream()
                .map(
                        test ->
                                DynamicTest.dynamicTest(
                                        fileName + "/" + test.getDescription(),
                                        () -> {
                                            boolean result =
                                                    unleash.isEnabled(
                                                            test.getToggleName(),
                                                            buildContext(test.getContext()));
                                            assertEquals(
                                                    test.getExpectedResult(),
                                                    result,
                                                    test.getDescription());
                                        }))
                .collect(Collectors.toList());
    }

    private List<DynamicTest> createVariantTests(String fileName)
            throws IOException, URISyntaxException {
        TestDefinition testDefinition = getTestDefinition(fileName);

        Unleash unleash = setupUnleash(testDefinition);

        // Create all test cases in testDefinition.
        return testDefinition.getVariantTests().stream()
                .map(
                        test ->
                                DynamicTest.dynamicTest(
                                        fileName + "/" + test.getDescription(),
                                        () -> {
                                            Variant result =
                                                    unleash.getVariant(
                                                            test.getToggleName(),
                                                            buildContext(test.getContext()));
                                            assertEquals(
                                                    test.getExpectedResult().getName(),
                                                    result.getName(),
                                                    test.getDescription());
                                            assertEquals(
                                                    test.getExpectedResult().isEnabled(),
                                                    result.isEnabled(),
                                                    test.getDescription());
                                            assertEquals(
                                                    test.getExpectedResult().getPayload(),
                                                    result.getPayload(),
                                                    test.getDescription());
                                            assertEquals(
                                                    test.getExpectedResult().isFeatureEnabled(),
                                                    result.isFeatureEnabled(),
                                                    test.getDescription());
                                        }))
                .collect(Collectors.toList());
    }

    private Unleash setupUnleash(TestDefinition testDefinition) throws URISyntaxException {

        ToggleBootstrapProvider bootstrapper =
                new ToggleBootstrapProvider() {
                    @Override
                    public String read() {
                        return testDefinition.getState().toString();
                    }
                };

        UnleashConfig config =
                UnleashConfig.builder()
                        .appName(testDefinition.getName())
                        .disableMetrics()
                        .disablePolling()
                        .toggleBootstrapProvider(bootstrapper)
                        .unleashAPI(new URI("http://notusedbutrequired:9999/api/"))
                        .build();

        DefaultUnleash defaultUnleash = new DefaultUnleash(config);

        return defaultUnleash;
    }

    private TestDefinition getTestDefinition(String fileName) throws IOException {
        Reader content = getFileReader("/client-specification/specifications/" + fileName);
        return new Gson().fromJson(content, TestDefinition.class);
    }

    private UnleashContext buildContext(UnleashContextDefinition context) {
        // TODO: All other properties!
        UnleashContext.Builder builder =
                UnleashContext.builder()
                        .userId(context.getUserId())
                        .sessionId(context.getSessionId())
                        .remoteAddress(context.getRemoteAddress())
                        .environment(context.getEnvironment())
                        .currentTime(DateParser.parseDate(context.getCurrentTime()))
                        .appName(context.getAppName());

        if (context.getProperties() != null) {
            context.getProperties().forEach(builder::addProperty);
        }

        return builder.build();
    }

    private Reader getFileReader(String filename) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(filename);
        assertNotNull(
                in,
                "Could not find test specification ("
                        + filename
                        + ").\n"
                        + "You must first run 'mvn test' to download the specifications files");
        InputStreamReader reader = new InputStreamReader(in);
        return new BufferedReader(reader);
    }
}
