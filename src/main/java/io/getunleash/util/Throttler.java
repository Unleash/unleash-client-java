package io.getunleash.util;

import static java.lang.Integer.max;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Throttler {
    private static final Logger LOGGER = LoggerFactory.getLogger(Throttler.class);
    private final int maxSkips;

    private final int intervalLength;
    private final AtomicInteger skips = new AtomicInteger(0);
    private final AtomicInteger failures = new AtomicInteger(0);

    private final URL target;

    public Throttler(int intervalLengthSeconds, int longestAcceptableIntervalSeconds, URL target) {
        this.maxSkips = max(longestAcceptableIntervalSeconds / max(intervalLengthSeconds, 1), 1);
        this.target = target;
        this.intervalLength = intervalLengthSeconds;
    }

    /**
     * We've had one successful call, so if we had 10 failures in a row, this will reduce the skips
     * down to 9, so that we gradually start polling more often, instead of doing max load
     * immediately after a sequence of errors.
     */
    public void decrementFailureCountAndResetSkips() {
        if (failures.get() > 0) {
            skips.set(Math.max(failures.decrementAndGet(), 0));
        }
    }

    /**
     * We've gotten the message to back off (usually a 429 or a 50x). If we have successive
     * failures, failure count here will be incremented higher and higher which will handle
     * increasing our backoff, since we set the skip count to the failure count after every reset
     */
    public void increaseSkipCount() {
        skips.set(Math.min(failures.incrementAndGet(), maxSkips));
    }

    /**
     * We've received an error code that we don't expect to change, which means we've already logged
     * an ERROR. To avoid hammering the server that just told us we did something wrong and to avoid
     * flooding the logs, we'll increase our skip count to maximum
     */
    public void maximizeSkips() {
        skips.set(maxSkips);
        failures.incrementAndGet();
    }

    public boolean performAction() {
        return skips.get() <= 0;
    }

    public void skipped() {
        skips.decrementAndGet();
    }

    public void handleHttpErrorCodes(int responseCode) {
        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            maximizeSkips();
            LOGGER.error(
                    "Client was not authorized to talk to the Unleash API at {}. Backing off to {} times our poll interval (of {} seconds) to avoid overloading server",
                    this.target,
                    maxSkips,
                    this.intervalLength);
        }
        if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            maximizeSkips();
            LOGGER.error(
                    "Server said that the endpoint at {} does not exist. Backing off to {} times our poll interval (of {} seconds) to avoid overloading server",
                    this.target,
                    maxSkips,
                    this.intervalLength);
        } else if (responseCode == 429) {
            increaseSkipCount();
            LOGGER.info(
                    "RATE LIMITED for the {}. time. Further backing off. Current backoff at {} times our interval (of {} seconds)",
                    failures.get(),
                    skips.get(),
                    this.intervalLength);
        } else if (responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
            increaseSkipCount();
            LOGGER.info(
                    "Server failed with a {} status code. Backing off. Current backoff at {} times our poll interval (of {} seconds)",
                    responseCode,
                    skips.get(),
                    this.intervalLength);
        }
    }

    public int getSkips() {
        return this.skips.get();
    }

    public int getFailures() {
        return this.failures.get();
    }
}
