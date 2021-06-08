/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getunleash.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** @author Luke Taylor */
class IpAddressMatcherTest {
    private final IpAddressMatcher v6matcher = new IpAddressMatcher("fe80::21f:5bff:fe33:bd68");
    private final IpAddressMatcher v4matcher = new IpAddressMatcher("192.168.1.104");

    @Test
    void ipv6MatcherMatchesIpv6Address() {
        assertThat(v6matcher.matches("fe80::21f:5bff:fe33:bd68")).isTrue();
    }

    @Test
    void ipv6MatcherDoesntMatchNull() {
        assertThat(v6matcher.matches(null)).isFalse();
        assertThat(new IpAddressMatcher("::1").matches(null)).isFalse();
    }

    @Test
    void ipv4MatcherDoesntMatchNull() {
        assertThat(v4matcher.matches(null)).isFalse();
        assertThat(new IpAddressMatcher("127.0.0.1").matches(null)).isFalse();
    }

    @Test
    void ipv6MatcherDoesntMatchIpv4Address() {
        assertThat(v6matcher.matches("192.168.1.104")).isFalse();
    }

    @Test
    void ipv4MatcherMatchesIpv4Address() {
        assertThat(v4matcher.matches("192.168.1.104")).isTrue();
    }

    @Test
    void ipv4SubnetMatchesCorrectly() {
        IpAddressMatcher matcher = new IpAddressMatcher("192.168.1.0/24");
        assertThat(matcher.matches("192.168.1.104")).isTrue();
        matcher = new IpAddressMatcher("192.168.1.128/25");
        assertThat(matcher.matches("192.168.1.104")).isFalse();
        assertThat(matcher.matches("192.168.1.159")).isTrue();
    }

    @Test
    void ipv6RangeMatches() {
        IpAddressMatcher matcher = new IpAddressMatcher("2001:DB8::/48");

        assertThat(matcher.matches("2001:DB8:0:0:0:0:0:0")).isTrue();
        assertThat(matcher.matches("2001:DB8:0:0:0:0:0:1")).isTrue();
        assertThat(matcher.matches("2001:DB8:0:FFFF:FFFF:FFFF:FFFF:FFFF")).isTrue();
        assertThat(matcher.matches("2001:DB8:0:ffff:ffff:ffff:ffff:ffff")).isTrue();
        assertThat(matcher.matches("2001:DB8:1:0:0:0:0:0")).isFalse();
    }

    @Test
    void zeroMaskMatchesAnything() {
        IpAddressMatcher matcher = new IpAddressMatcher("0.0.0.0/0");

        assertThat(matcher.matches("123.4.5.6")).isTrue();
        assertThat(matcher.matches("192.168.0.159")).isTrue();

        matcher = new IpAddressMatcher("192.168.0.159/0");
        assertThat(matcher.matches("123.4.5.6")).isTrue();
        assertThat(matcher.matches("192.168.0.159")).isTrue();
    }
}
