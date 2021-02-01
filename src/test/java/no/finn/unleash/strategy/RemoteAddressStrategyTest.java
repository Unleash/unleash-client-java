package no.finn.unleash.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
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

class RemoteAddressStrategyTest {
    private static final String FIRST_IPV4 = "127.0.0.1";
    private static final String SECOND_IPV4 = "10.0.0.1";
    private static final String THIRD_IPV4 = "196.0.0.1";
    private static final String FOURTH_IPV4 = "192.168.42.23";
    private static final String IPV4_SUBNET = "192.168.0.0/16";
    private static final List<String> ALL_IPV4 =
            Arrays.asList(FIRST_IPV4, SECOND_IPV4, THIRD_IPV4, IPV4_SUBNET);

    private static final String FIRST_IPV6 = "::1";
    private static final String SECOND_IPV6 = "2001:DB8:0:0:0:0:0:1";
    private static final String THIRD_IPV6 = "2001:DB8::1";
    private static final String IPV6_SUBNET = "2001:DB8::/48";
    private static final List<String> ALL_IPV6 =
            Arrays.asList(FIRST_IPV6, SECOND_IPV6, THIRD_IPV6, IPV6_SUBNET);

    private static final List<String> ALL =
            ImmutableList.<String>builder().addAll(ALL_IPV4).addAll(ALL_IPV6).build();

    private RemoteAddressStrategy strategy;

    static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of(FIRST_IPV4, FIRST_IPV4, true),
                Arguments.of(FIRST_IPV4, String.join(",", ALL_IPV4), true),
                Arguments.of(SECOND_IPV4, String.join(",", ALL_IPV4), true),
                Arguments.of(THIRD_IPV4, String.join(",", ALL_IPV4), true),
                Arguments.of(FIRST_IPV4, String.join(", ", ALL_IPV4), true),
                Arguments.of(SECOND_IPV4, String.join(", ", ALL_IPV4), true),
                Arguments.of(THIRD_IPV4, String.join(", ", ALL_IPV4), true),
                Arguments.of(SECOND_IPV4, String.join(",  ", ALL_IPV4), true),
                Arguments.of(SECOND_IPV4, String.join(".", ALL_IPV4), false),
                Arguments.of(FIRST_IPV4, SECOND_IPV4, false),
                Arguments.of(FOURTH_IPV4, String.join(",", ALL_IPV4), true),
                Arguments.of(FIRST_IPV4, IPV4_SUBNET, false),
                Arguments.of(FIRST_IPV4, null, false),
                Arguments.of(FIRST_IPV4, "", false),
                Arguments.of(null, String.join(",", ALL_IPV4), false),
                Arguments.of(FIRST_IPV6, FIRST_IPV6, true),
                Arguments.of(FIRST_IPV6, String.join(",", ALL_IPV6), true),
                Arguments.of(SECOND_IPV6, String.join(",", ALL_IPV6), true),
                Arguments.of(THIRD_IPV6, String.join(",", ALL_IPV6), true),
                Arguments.of(FIRST_IPV6, String.join(", ", ALL_IPV6), true),
                Arguments.of(SECOND_IPV6, String.join(", ", ALL_IPV6), true),
                Arguments.of(THIRD_IPV6, String.join(", ", ALL_IPV6), true),
                Arguments.of(SECOND_IPV6, String.join(",  ", ALL_IPV6), true),
                Arguments.of(SECOND_IPV6, String.join(".", ALL_IPV6), false),
                Arguments.of(FIRST_IPV6, SECOND_IPV6, false),
                Arguments.of(FIRST_IPV6, IPV6_SUBNET, false),
                Arguments.of(FIRST_IPV6, null, false),
                Arguments.of(FIRST_IPV6, "", false),
                Arguments.of(null, String.join(".", ALL_IPV6), false),
                Arguments.of(FIRST_IPV4, String.join(".", ALL), false),
                Arguments.of(FIRST_IPV6, String.join(".", ALL), false));
    }

    @BeforeEach
    void setUp() {
        strategy = new RemoteAddressStrategy();
    }

    @Test
    void should_have_a_name() {
        assertThat(strategy.getName()).isEqualTo("remoteAddress");
    }

    @ParameterizedTest
    @MethodSource("data")
    void test_all_combinations(String actualIp, String parameterString, boolean expected) {
        UnleashContext context = UnleashContext.builder().remoteAddress(actualIp).build();
        Map<String, String> parameters = setupParameterMap(parameterString);

        assertThat(strategy.isEnabled(parameters, context)).isEqualTo(expected);
    }

    private Map<String, String> setupParameterMap(String ipString) {
        if (ipString == null) {
            return Collections.emptyMap();
        }

        Map<String, String> parameters = new HashMap<>();
        parameters.put(RemoteAddressStrategy.PARAM, ipString);
        return parameters;
    }
}
