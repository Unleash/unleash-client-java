package no.finn.unleash.strategy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import no.finn.unleash.UnleashContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RemoteAddressStrategyTest {
    private static final String FIRST_IP = "127.0.0.1";
    private static final String SECOND_IP = "10.0.0.1";
    private static final String THIRD_IP = "196.0.0.1";
    private static final List<String> ALL = Arrays.asList(FIRST_IP, SECOND_IP, THIRD_IP);

    private RemoteAddressStrategy strategy;

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(FIRST_IP, FIRST_IP, true),
                Arguments.of(FIRST_IP, String.join(",", ALL), true),
                Arguments.of(SECOND_IP, String.join(",", ALL), true),
                Arguments.of(THIRD_IP, String.join(",", ALL), true),
                Arguments.of(FIRST_IP, String.join(", ", ALL), true),
                Arguments.of(SECOND_IP, String.join(", ", ALL), true),
                Arguments.of(THIRD_IP, String.join(", ", ALL), true),
                Arguments.of(SECOND_IP, String.join(",  ", ALL), true),
                Arguments.of(SECOND_IP, String.join(".", ALL), false),
                Arguments.of(FIRST_IP, SECOND_IP, false));
    }

    @BeforeEach
    public void setUp() {
        strategy = new RemoteAddressStrategy();
    }

    @Test
    public void should_have_a_name() {
        assertThat(strategy.getName(), is("remoteAddress"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void test_all_combinations(String actualIp, String parameterString, boolean expected) {
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