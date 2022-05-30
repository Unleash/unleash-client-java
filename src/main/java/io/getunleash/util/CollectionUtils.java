package io.getunleash.util;

import io.getunleash.lang.Nullable;
import java.util.Collection;

public class CollectionUtils {

    public static boolean isNullOrEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
