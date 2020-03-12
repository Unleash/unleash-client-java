package no.finn.unleash.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class UnleashScheduledExecutorImpl implements UnleashScheduledExecutor {

    private static final Logger LOG = LogManager.getLogger(UnleashScheduledExecutorImpl.class);

    private static UnleashScheduledExecutorImpl INSTANCE;

    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ExecutorService executorService;

    public UnleashScheduledExecutorImpl() {
        ThreadFactory threadFactory = runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("unleash-api-executor");
            thread.setDaemon(true);
            return thread;
        };

        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, threadFactory);
        this.scheduledThreadPoolExecutor.setRemoveOnCancelPolicy(true);

        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    public static synchronized UnleashScheduledExecutorImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UnleashScheduledExecutorImpl();
        }
        return INSTANCE;
    }

    @Override
    public ScheduledFuture setInterval(Runnable command,
                                                      long initialDelaySec,
                                                      long periodSec) {
        try {
            return scheduledThreadPoolExecutor.scheduleAtFixedRate(command, initialDelaySec, periodSec, TimeUnit.SECONDS);
        } catch (RejectedExecutionException ex) {
            LOG.error("Unleash background task crashed", ex);
            return null;
        }

    }

    @Override
    public Future<Void> scheduleOnce(Runnable runnable) {
        return (Future<Void>) executorService.submit(runnable);
    }

    @Override
    public void shutdown() {
        this.scheduledThreadPoolExecutor.shutdown();
    }
}
