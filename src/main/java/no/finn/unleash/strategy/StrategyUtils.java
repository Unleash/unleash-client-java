package no.finn.unleash.strategy;

import com.sangupta.murmur.Murmur3;

public final class StrategyUtils {
    private static final int ONE_HUNDRED = 100;

    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static boolean isNumeric(final CharSequence cs) {
        if (isEmpty(cs)) {
            return false;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (Character.isDigit(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Takes to string inputs concat them, produce a hash and return a normalized value between 0
     * and 100;
     *
     * @param identifier
     * @param groupId
     * @return
     */
    public static int getNormalizedNumber(String identifier, String groupId) {
        return getNormalizedNumber(identifier, groupId, ONE_HUNDRED);
    }

    public static int getNormalizedNumber(String identifier, String groupId, int normalizer) {
        byte[] value = (groupId + ':' + identifier).getBytes();
        long hash = Murmur3.hash_x86_32(value, value.length, 0);
        return (int) (hash % normalizer) + 1;
    }

    /**
     * Takes a numeric string value and converts it to a integer between 0 and 100.
     *
     * <p>returns 0 if the string is not numeric.
     *
     * @param percentage - A numeric string value
     * @return a integer between 0 and 100
     */
    public static int getPercentage(String percentage) {
        if (isNotEmpty(percentage) && isNumeric(percentage)) {
            int p = Integer.parseInt(percentage);
            return p;
        } else {
            return 0;
        }
    }
}
