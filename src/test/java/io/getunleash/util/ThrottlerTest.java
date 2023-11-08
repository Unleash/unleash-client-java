package io.getunleash.util;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class ThrottlerTest {

    @Test
    public void shouldNeverDecrementFailuresOrSkipsBelowZero() throws MalformedURLException {
        Throttler throttler = new Throttler(10, 300, URI.create("https://localhost:1500/api").toURL());
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        assertThat(throttler.getSkips()).isEqualTo(0);
        assertThat(throttler.getFailures()).isEqualTo(0);
    }

    @Test
    public void setToMaxShouldReduceDownEventually() throws MalformedURLException {
        Throttler throttler = new Throttler(150, 300,URI.create("https://localhost:1500/api").toURL());
        throttler.handleHttpErrorCodes(404);
        assertThat(throttler.getSkips()).isEqualTo(2);
        assertThat(throttler.getFailures()).isEqualTo(1);
        throttler.skipped();
        assertThat(throttler.getSkips()).isEqualTo(1);
        assertThat(throttler.getFailures()).isEqualTo(1);
        throttler.skipped();
        assertThat(throttler.getSkips()).isEqualTo(0);
        assertThat(throttler.getFailures()).isEqualTo(1);
        throttler.decrementFailureCountAndResetSkips();
        assertThat(throttler.getSkips()).isEqualTo(0);
        assertThat(throttler.getFailures()).isEqualTo(0);
        throttler.decrementFailureCountAndResetSkips();
        assertThat(throttler.getSkips()).isEqualTo(0);
        assertThat(throttler.getFailures()).isEqualTo(0);
    }
    @Test
    public void handleIntermittentFailures() throws MalformedURLException {
        Throttler throttler = new Throttler(50, 300, URI.create("https://localhost:1500/api").toURL());
        throttler.handleHttpErrorCodes(429);
        throttler.handleHttpErrorCodes(429);
        throttler.handleHttpErrorCodes(503);
        throttler.handleHttpErrorCodes(429);
        assertThat(throttler.getSkips()).isEqualTo(4);
        assertThat(throttler.getFailures()).isEqualTo(4);
        throttler.decrementFailureCountAndResetSkips();
        assertThat(throttler.getSkips()).isEqualTo(3);
        assertThat(throttler.getFailures()).isEqualTo(3);
        throttler.handleHttpErrorCodes(429);
        assertThat(throttler.getSkips()).isEqualTo(4);
        assertThat(throttler.getFailures()).isEqualTo(4);
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        throttler.decrementFailureCountAndResetSkips();
        assertThat(throttler.getSkips()).isEqualTo(0);
        assertThat(throttler.getFailures()).isEqualTo(0);

    }
}
