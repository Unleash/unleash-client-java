package io.getunleash.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.UnleashContext;
import io.getunleash.Variant;
import io.getunleash.strategy.constraints.DateParser;
import io.getunleash.util.UnleashConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
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
import org.junit.jupiter.api.extension.RegisterExtension;

public class ClientSpecificationTest {

    @RegisterExtension
    static WireMockExtension serverMock =
            WireMockExtension.newInstance()
                    .configureStaticDsl(true)
                    .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
                    .build();

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
                                        }))
                .collect(Collectors.toList());
    }

    private Unleash setupUnleash(TestDefinition testDefinition) throws URISyntaxException {
        mockUnleashAPI(testDefinition);

        // Required because the client is available before it may have had the chance to talk with
        // the API
        String backupFile = writeUnleashBackup(testDefinition);

        // Set-up a unleash instance, using mocked API and backup-file
        UnleashConfig config =
                UnleashConfig.builder()
                        .appName(testDefinition.getName())
                        .unleashAPI(new URI("http://localhost:" + serverMock.getPort() + "/api/"))
                        .synchronousFetchOnInitialisation(true)
                        .backupFile(backupFile)
                        .build();

        return new DefaultUnleash(config);
    }

    private void mockUnleashAPI(TestDefinition definition) {
        stubFor(
                get(urlEqualTo("/api/client/features"))
                        .withHeader("Accept", equalTo("application/json"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(definition.getState().toString())));
        stubFor(post(urlEqualTo("/api/client/register")).willReturn(aResponse().withStatus(200)));
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

    private String writeUnleashBackup(TestDefinition definition) {
        String backupFile =
                System.getProperty("java.io.tmpdir")
                        + File.separatorChar
                        + "unleash-test-"
                        + definition.getName()
                        + ".json";

        // TODO: we can probably drop this after introduction of `synchronousFetchOnInitialisation`.
        try (FileWriter writer = new FileWriter(backupFile)) {
            writer.write(definition.getState().toString());
        } catch (IOException e) {
            System.out.println("Unable to write toggles to file");
        }

        return backupFile;
    }
}
