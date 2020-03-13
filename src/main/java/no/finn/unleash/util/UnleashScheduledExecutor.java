package no.finn.unleash.util;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;

public interface UnleashScheduledExecutor {
    ScheduledFuture setInterval(
            Runnable command, long initialDelaySec, long periodSec) throws RejectedExecutionException;

    Future<Void> scheduleOnce(Runnable runnable);

    default public void shutdown() {
    }
}
