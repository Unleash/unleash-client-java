package no.finn.unleash.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import no.finn.unleash.FeatureToggle;
import no.finn.unleash.UnleashException;

public final class FeatureToggleRepository implements ToggleRepository {
    private static final Logger LOG = LogManager.getLogger();

    private static final ScheduledThreadPoolExecutor TIMER = new ScheduledThreadPoolExecutor(
            1,
            new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable r) {
                    Thread thread = Executors.defaultThreadFactory().newThread(r);
                    thread.setName("unleash-toggle-repository");
                    thread.setDaemon(true);
                    return thread;
                }
            });

    static {
        TIMER.setRemoveOnCancelPolicy(true);
    }

    private final ToggleBackupHandler toggleBackupHandler;
    private final ToggleFetcher toggleFetcher;

    private ToggleCollection toggleCollection;

    public FeatureToggleRepository(ToggleFetcher toggleFetcher, ToggleBackupHandler toggleBackupHandler) {
        this(toggleFetcher, toggleBackupHandler, 10L);
    }

    public FeatureToggleRepository(ToggleFetcher toggleFetcher, ToggleBackupHandler toggleBackupHandler, long pollIntervalSeconds) {
        this.toggleBackupHandler = toggleBackupHandler;
        this.toggleFetcher = toggleFetcher;

        toggleCollection = toggleBackupHandler.read();
        startBackgroundPolling(pollIntervalSeconds);
    }

    private ScheduledFuture startBackgroundPolling(long pollIntervalSeconds) {
        try {
            return TIMER.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        Response response = toggleFetcher.fetchToggles();
                        if (response.getStatus() == Response.Status.CHANGED) {
                            toggleCollection = response.getToggleCollection();
                            toggleBackupHandler.write(response.getToggleCollection());
                        }
                    } catch (UnleashException e) {
                        LOG.warn("Could not refresh feature toggles", e);
                    }
                }
            }, 0, pollIntervalSeconds, TimeUnit.SECONDS);
        } catch (RejectedExecutionException ex) {
            LOG.error("Unleash background task crashed", ex);
            return null;
        }
    }

    @Override
    public FeatureToggle getToggle(String name) {
        return toggleCollection.getToggle(name);
    }
}
