package io.getunleash.util;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import io.getunleash.lang.Nullable;

public interface UnleashScheduledExecutor {
    @Nullable
    ScheduledFuture setInterval(Runnable command, long initialDelaySec, long periodSec)
            throws RejectedExecutionException;

    Future<Void> scheduleOnce(Runnable runnable);

    public default void shutdown() {}
}
