package no.finn.unleash.util;

import no.finn.unleash.lang.Nullable;

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
