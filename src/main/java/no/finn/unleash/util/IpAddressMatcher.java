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
package no.finn.unleash.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.regex.Pattern;
import no.finn.unleash.lang.Nullable;

/**
 * Matches a request based on IP Address or subnet mask matching against the remote address.
 *
 * <p>Both IPv6 and IPv4 addresses are supported, but a matcher which is configured with an IPv4
 * address will never match a request which returns an IPv6 address, and vice-versa.
 *
 * @author Luke Taylor
 */
public final class IpAddressMatcher {
    private static final Pattern SPLITTER = Pattern.compile("/");
    private final int nMaskBits;
    @Nullable private final InetAddress requiredAddress;

    /**
     * Takes a specific IP address or a range specified using the IP/Netmask (e.g. 192.168.1.0/24 or
     * 202.24.0.0/14).
     *
     * @param ipAddress the address or range of addresses from which the request must come.
     */
    public IpAddressMatcher(@Nullable String ipAddress) {
        final String trimmedIpAddress = ipAddress == null ? "" : ipAddress.trim();

        if (trimmedIpAddress.indexOf('/') > 0) {
            String[] addressAndMask = SPLITTER.split(trimmedIpAddress, -1);
            requiredAddress = parseAddress(addressAndMask[0]);
            nMaskBits = Integer.parseInt(addressAndMask[1]);
        } else {
            requiredAddress = parseAddress(trimmedIpAddress);
            nMaskBits = -1;
        }
    }

    public boolean matches(@Nullable String address) {
        if (address == null || address.isEmpty() || requiredAddress == null) {
            return false;
        }

        InetAddress remoteAddress = parseAddress(address);

        if (remoteAddress == null || !requiredAddress.getClass().equals(remoteAddress.getClass())) {
            return false;
        }

        if (nMaskBits < 0) {
            return remoteAddress.equals(requiredAddress);
        }

        byte[] remAddr = remoteAddress.getAddress();
        byte[] reqAddr = requiredAddress.getAddress();

        int oddBits = nMaskBits % 8;
        int nMaskBytes = nMaskBits / 8 + (oddBits == 0 ? 0 : 1);
        byte[] mask = new byte[nMaskBytes];

        Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte) 0xFF);

        if (oddBits != 0) {
            int finalByte = (1 << oddBits) - 1;
            finalByte <<= 8 - oddBits;
            mask[mask.length - 1] = (byte) finalByte;
        }

        for (int i = 0; i < mask.length; i++) {
            if ((remAddr[i] & mask[i]) != (reqAddr[i] & mask[i])) {
                return false;
            }
        }

        return true;
    }

    private @Nullable InetAddress parseAddress(@Nullable String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        try {
            return InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Failed to parse address " + address, e);
        }
    }
}
