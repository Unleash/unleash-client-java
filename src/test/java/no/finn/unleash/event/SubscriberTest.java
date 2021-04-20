package no.finn.unleash.event;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static no.finn.unleash.repository.FeatureToggleResponse.Status.UNAVAILABLE;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.jenspiegsa.wiremockextension.ConfigureWireMock;
import com.github.jenspiegsa.wiremockextension.InjectServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.jenspiegsa.wiremockextension.WireMockSettings;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import java.util.ArrayList;
import java.util.List;
import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.SynchronousTestExecutor;
import no.finn.unleash.Unleash;
import no.finn.unleash.UnleashException;
import no.finn.unleash.metric.ClientMetrics;
import no.finn.unleash.metric.ClientRegistration;
import no.finn.unleash.repository.FeatureToggleResponse;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleBootstrapHandler;
import no.finn.unleash.util.UnleashConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(WireMockExtension.class)
@WireMockSettings(failOnUnmatchedRequests = false)
public class SubscriberTest {

    @ConfigureWireMock Options options = wireMockConfig().dynamicPort();

    @InjectServer WireMockServer serverMock;

    private TestSubscriber testSubscriber = new TestSubscriber();
    private UnleashConfig unleashConfig;

    @BeforeEach
    void setup() {
        unleashConfig =
                new UnleashConfig.Builder()
                        .appName(SubscriberTest.class.getSimpleName())
                        .instanceId(SubscriberTest.class.getSimpleName())
                        .synchronousFetchOnInitialisation(true)
                        .unleashAPI("http://localhost:" + serverMock.port())
                        .subscriber(testSubscriber)
                        .scheduledExecutor(new SynchronousTestExecutor())
                        .build();
    }

    @Test
    void subscriberAreNotified() {
        Unleash unleash = new DefaultUnleash(unleashConfig);

        unleash.isEnabled("myFeature");
        unleash.isEnabled("myFeature");
        unleash.isEnabled("myFeature");

        assertThat(testSubscriber.togglesFetchedCounter).isEqualTo(2); // one forced, one scheduled
        assertThat(testSubscriber.status).isEqualTo(UNAVAILABLE);
        assertThat(testSubscriber.toggleEvalutatedCounter).isEqualTo(3);
        assertThat(testSubscriber.toggleName).isEqualTo("myFeature");
        assertThat(testSubscriber.toggleEnabled).isFalse();
        assertThat(testSubscriber.errors).hasSize(2);

//        assertThat(testSubscriber.events).filteredOn(e -> e instanceof ToggleBootstrapHandler.ToggleBootstrapRead).hasSize(1);
        assertThat(testSubscriber.events).filteredOn(e -> e instanceof UnleashReady).hasSize(1);
        assertThat(testSubscriber.events).filteredOn(e -> e instanceof ToggleEvaluated).hasSize(3);
        assertThat(testSubscriber.events).filteredOn(e -> e instanceof FeatureToggleResponse).hasSize(2);
        assertThat(testSubscriber.events).filteredOn(e -> e instanceof ClientRegistration).hasSize(1);
        assertThat(testSubscriber.events).filteredOn(e -> e instanceof ClientMetrics).hasSize(1);
        assertThat(testSubscriber.events).hasSize(8);

    }

    private class TestSubscriber implements UnleashSubscriber {

        private int togglesFetchedCounter;
        private FeatureToggleResponse.Status status;

        private int toggleEvalutatedCounter;
        private String toggleName;
        private boolean toggleEnabled;

        private List<UnleashEvent> events = new ArrayList<>();
        private List<UnleashException> errors = new ArrayList<>();

        @Override
        public void on(UnleashEvent unleashEvent) {
            this.events.add(unleashEvent);
        }

        @Override
        public void onError(UnleashException unleashException) {
            this.errors.add(unleashException);
        }

        @Override
        public void toggleEvaluated(ToggleEvaluated toggleEvaluated) {
            this.toggleEvalutatedCounter++;
            this.toggleName = toggleEvaluated.getToggleName();
            this.toggleEnabled = toggleEvaluated.isEnabled();
        }

        @Override
        public void togglesFetched(FeatureToggleResponse toggleResponse) {
            this.togglesFetchedCounter++;
            this.status = toggleResponse.getStatus();
        }
    }
}
