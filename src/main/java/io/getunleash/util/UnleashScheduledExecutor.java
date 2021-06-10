package io.getunleash.util;

import io.getunleash.lang.Nullable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;

public interface UnleashScheduledExecutor {
    @Nullable
    ScheduledFuture setInterval(Runnable command, long initialDelaySec, long periodSec)
            throws RejectedExecutionException;

    Future<Void> scheduleOnce(Runnable runnable);

    public default void shutdown() {}
}
