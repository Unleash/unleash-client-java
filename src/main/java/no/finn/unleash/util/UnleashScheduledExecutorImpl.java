package no.finn.unleash.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class UnleashScheduledExecutorImpl implements UnleashScheduledExecutor {
    private static final Logger LOG = LogManager.getLogger(UnleashScheduledExecutorImpl.class);

    private final ScheduledThreadPoolExecutor timer;

    public UnleashScheduledExecutorImpl() {
        this.timer = new ScheduledThreadPoolExecutor(
                1,
                r -> {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
                    thread.setName("unleash-api-executor");
                    thread.setDaemon(true);
                    return thread;
                });
        this.timer.setRemoveOnCancelPolicy(true);
    }

    @Override
    public ScheduledFuture setInterval(Runnable command,
                                                      long initialDelaySec,
                                                      long periodSec) {
        try {
            return timer.scheduleAtFixedRate(command, initialDelaySec, periodSec, TimeUnit.SECONDS);
        } catch (RejectedExecutionException ex) {
            LOG.error("Unleash background task crashed", ex);
            return null;
        }

    }
}
