package no.finn.unleash.strategy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class ApplicationHostnameStrategyTest {

    @After
    public void remove_hostname_property() {
        System.getProperties().remove("hostname");
    }

    @Test
    public void should_be_disabled_if_no_HostNames_in_params() {
        Strategy strategy = new ApplicationHostnameStrategy();
        Map<String, String> params = new HashMap<>();
        params.put("hostNames", null);

        assertFalse(strategy.isEnabled(params));
    }

    @Test
    public void should_be_disabled_if_hostname_not_in_list() {
        Strategy strategy = new ApplicationHostnameStrategy();

        Map<String, String> params = new HashMap<>();
        params.put("hostNames", "MegaHost,MiniHost, happyHost");

        assertFalse(strategy.isEnabled(params));
    }

    @Test
    public void should_be_enabled_for_hostName(){
        String hostName  = "my-super-host";
        System.setProperty("hostname", hostName);

        Strategy strategy = new ApplicationHostnameStrategy();

        Map<String, String> params = new HashMap<>();
        params.put("hostNames", "MegaHost," + hostName + ",MiniHost, happyHost");
        assertTrue(strategy.isEnabled(params));
    }

    @Test
    public void should_handle_weird_casing(){
        String hostName  = "my-super-host";
        System.setProperty("hostname", hostName);

        Strategy strategy = new ApplicationHostnameStrategy();

        Map<String, String> params = new HashMap<>();

        params.put("hostNames", "MegaHost," + hostName.toUpperCase() + ",MiniHost, happyHost");
        assertTrue(strategy.isEnabled(params));
    }

    @Test
    public void so_close_but_no_cigar(){
        String hostName  = "my-super-host";
        System.setProperty("hostname", hostName);

        Strategy strategy = new ApplicationHostnameStrategy();

        Map<String, String> params = new HashMap<>();

        params.put("hostNames", "MegaHost, MiniHost, SuperhostOne");
        assertFalse(strategy.isEnabled(params));
    }

    @Test
    public void should_be_enabled_for_InetAddress() throws UnknownHostException {
        String hostName  = InetAddress.getLocalHost().getHostName();
        System.setProperty("hostname", hostName);

        Strategy strategy = new ApplicationHostnameStrategy();

        Map<String, String> params = new HashMap<>();
        params.put("hostNames", "MegaHost," + hostName + ",MiniHost, happyHost");
        assertTrue(strategy.isEnabled(params));
    }

    @Test
    public void null_test(){
        Strategy strategy = new ApplicationHostnameStrategy();
        assertFalse(strategy.isEnabled(new HashMap<>()));
    }

}