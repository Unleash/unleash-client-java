package io.getunleash.strategy;

import com.google.common.collect.ImmutableList;
import io.getunleash.ActivationStrategy;
import io.getunleash.DefaultUnleash;
import io.getunleash.FeatureToggle;
import io.getunleash.repository.UnleashEngineStateHandler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ApplicationHostnameStrategyTest {

    private DefaultUnleash engine;
    private UnleashEngineStateHandler stateHandler;

    @BeforeEach
    void init() {
        UnleashConfig config =
            new UnleashConfig.Builder()
                .appName("test")
                .unleashAPI("http://localhost:4242/api/")
                .environment("test")
                .scheduledExecutor(mock(UnleashScheduledExecutor.class))
                .build();


        engine = new DefaultUnleash(config);
        stateHandler = new UnleashEngineStateHandler(engine);
    }

    @AfterEach
    public void remove_hostname_property() {
        System.getProperties().remove("hostname");
    }

    @Test
    public void should_be_disabled_if_no_HostNames_in_params() {
        Map<String, String> params = new HashMap<>();
        params.put("hostNames", null);

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("applicationHostname", params))
        ));
        assertFalse(engine.isEnabled("test"));
    }

    @Test
    public void should_be_disabled_if_hostname_not_in_list() {
        Map<String, String> params = new HashMap<>();
        params.put("hostNames", "MegaHost,MiniHost, happyHost");

        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("applicationHostname", params))
        ));
        assertFalse(engine.isEnabled("test"));
    }

    @Test
    public void so_close_but_no_cigar() {
        String hostName = "my-super-host";
        System.setProperty("hostname", hostName);

        Map<String, String> params = new HashMap<>();

        params.put("hostNames", "MegaHost, MiniHost, SuperhostOne");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("applicationHostname", params))
        ));
        assertFalse(engine.isEnabled("test"));
    }

    @Test
    public void should_be_enabled_for_InetAddress() throws UnknownHostException {
        String hostName = InetAddress.getLocalHost().getHostName();
        System.setProperty("hostname", hostName);

        Map<String, String> params = new HashMap<>();
        params.put("hostNames", "MegaHost," + hostName + ",MiniHost, happyHost");
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("applicationHostname", params))
        ));
        assertTrue(engine.isEnabled("test"));
    }

    @Test
    public void null_test() {
        stateHandler.setState(new FeatureToggle(
            "test",
            true,
            ImmutableList.of(new ActivationStrategy("applicationHostname", new HashMap<>()))
        ));
        assertFalse(engine.isEnabled("test"));
    }
}
