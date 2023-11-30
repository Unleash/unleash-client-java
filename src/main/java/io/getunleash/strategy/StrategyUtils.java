package io.getunleash.strategy;

import com.sangupta.murmur.Murmur3;

public final class StrategyUtils {
    private static final int ONE_HUNDRED = 100;

    /**
     * Takes to string inputs concat them, produce a hash and return a normalized value between 0
     * and 100;
     *
     * @param identifier
     * @param groupId
     * @return
     */
    public static int getNormalizedNumber(String identifier, String groupId, long seed) {
        byte[] value = (groupId + ':' + identifier).getBytes();
        long hash = Murmur3.hash_x86_32(value, value.length, seed);
        return (int) (hash % ONE_HUNDRED) + 1;
    }
}
