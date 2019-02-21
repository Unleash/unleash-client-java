package no.finn.unleash;

import no.finn.unleash.util.UnleashScheduledExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Delayed;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SynchronousTestExecutor implements UnleashScheduledExecutor {

    private static final Logger LOG = LogManager.getLogger(SynchronousTestExecutor.class);

    @Override
    public ScheduledFuture setInterval(Runnable command, long initialDelaySec, long periodSec) throws RejectedExecutionException {
        LOG.warn("i will only do this once");
        return scheduleOnce(command);
    }

    @Override
    public ScheduledFuture scheduleOnce(Runnable runnable) {
        runnable.run();
        return new AlreadyCompletedScheduledFuture();
    }

    private static class AlreadyCompletedScheduledFuture implements ScheduledFuture<Void> {
        @Override
        public long getDelay(TimeUnit timeUnit) {
            return 0;
        }

        @Override
        public int compareTo(Delayed delayed) {
            return 0;
        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public Void get() {
            return null;
        }

        @Override
        public Void get(long l, TimeUnit timeUnit) {
            return null;
        }
    }

}
