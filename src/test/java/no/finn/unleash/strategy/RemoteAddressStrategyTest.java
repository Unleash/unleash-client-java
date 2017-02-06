package no.finn.unleash.strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.finn.unleash.UnleashContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class RemoteAddressStrategyTest {
    private static final String FIRST_IP = "127.0.0.1";
    private static final String SECOND_IP = "10.0.0.1";
    private static final String THIRD_IP = "196.0.0.1";
    private static final List<String> ALL = Arrays.asList(FIRST_IP, SECOND_IP, THIRD_IP);

    @Parameter(value=0)
    public String actualIp;

    @Parameter(value=1)
    public String parameterString;

    @Parameter(value=2)
    public boolean expected;

    private RemoteAddressStrategy strategy;

    @Parameters(name="{index}: actualIp: {0}, parameter: {1}, expected: {2}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {FIRST_IP, FIRST_IP, true},
                {FIRST_IP, String.join(",", ALL), true},
                {SECOND_IP, String.join(",", ALL), true},
                {THIRD_IP, String.join(",", ALL), true},
                {FIRST_IP, String.join(", ", ALL), true},
                {SECOND_IP, String.join(", ", ALL), true},
                {THIRD_IP, String.join(", ", ALL), true},
                {SECOND_IP, String.join(",  ", ALL), true},
                {SECOND_IP, String.join(".", ALL), false},
                {FIRST_IP, SECOND_IP, false},
        });
    }

    @Before
    public void setUp() {
        strategy = new RemoteAddressStrategy();
    }

    @Test
    public void should_have_a_name() {
        assertThat(strategy.getName(), is("remoteAddress"));
    }

    @Test
    public void test() {
        UnleashContext context = UnleashContext.builder().remoteAddress(actualIp).build();
        Map<String, String> parameters = setupParameterMap(parameterString);

        assertThat(strategy.isEnabled(parameters, context), is(expected));
    }

    private Map<String, String> setupParameterMap(String ipString) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(RemoteAddressStrategy.PARAM, ipString);
        return parameters;
    }
}