package no.finn.unleash.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jenspiegsa.mockitoextension.ConfigureWireMock;
import com.github.jenspiegsa.mockitoextension.InjectServer;
import com.github.jenspiegsa.mockitoextension.WireMockExtension;
import com.github.jenspiegsa.mockitoextension.WireMockSettings;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashContext;
import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(WireMockExtension.class)
@WireMockSettings(failOnUnmatchedRequests = false)
public class ClientSpecificationTest {

    @ConfigureWireMock
    Options options = wireMockConfig()
            .dynamicPort();

    @InjectServer
    WireMockServer serverMock;

    @TestFactory
    public Stream<DynamicTest> clientSpecification() throws IOException, URISyntaxException {
        Reader content = getFileReader("/client-specification/specifications/index.json");
        List<String> testDefinitions = new Gson().fromJson(content, new TypeToken<List<String>>(){}.getType());

        List<DynamicTest> tests = new ArrayList<>();
        for(String name : testDefinitions) {
            tests.addAll(createTests(name));
        }
        return tests.stream();
    }

    private List<DynamicTest> createTests(String fileName) throws IOException, URISyntaxException {
        Reader content = getFileReader("/client-specification/specifications/"+ fileName);
        TestDefinition definition =  new Gson().fromJson(content, TestDefinition.class);

        return definition.getTests().stream()
                .map(test -> DynamicTest.dynamicTest(fileName + "/" + test.getDescription(), () -> {
                    stubFor(get(urlEqualTo("/api/client/features"))
                            .withHeader("Accept", equalTo("application/json"))
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(definition.getState().toString())));

                    URI unleashURI = new URI("http://localhost:"+ serverMock.port() + "/api/");
                    UnleashConfig config = UnleashConfig.builder()
                            .appName(definition.getName())
                            .unleashAPI(unleashURI)
                            .fetchTogglesInterval(1)
                            .build();

                    Unleash unleash = new DefaultUnleash(config);

                    boolean result = unleash.isEnabled(test.getToggleName(), buildContext(test));

                    assertEquals(test.getExpectedResult(), result, test.getDescription());
                }))
                .collect(Collectors.toList());
    }

    private UnleashContext buildContext(TestCase test) {
        //TODO: All other properties!
        UnleashContextDefinition context = test.getContext();
        return UnleashContext.builder()
                .userId(context.getUserId())
                .sessionId(context.getSessionId())
                .remoteAddress(context.getRemoteAddress())
                .build();
    }

    private Reader getFileReader(String filename) throws IOException {
        InputStream in = this.getClass().getResourceAsStream(filename);
        assertNotNull(in, "Could not find test specification ("+filename +").\n" +
                "You must first run 'mvn test' to download the specifications files");
        InputStreamReader reader = new InputStreamReader(in);
        return new BufferedReader(reader);
    }
}
