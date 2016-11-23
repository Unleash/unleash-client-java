package no.finn.unleash.util;

import java.util.concurrent.*;

public interface UnleashScheduledExecutor {
       ScheduledFuture setInterval(
               Runnable command, long initialDelaySec, long periodSec) throws RejectedExecutionException;
}
