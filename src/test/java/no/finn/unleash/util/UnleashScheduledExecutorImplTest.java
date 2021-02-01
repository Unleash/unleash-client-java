package no.finn.unleash.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UnleashScheduledExecutorImplTest {

    private UnleashScheduledExecutorImpl unleashScheduledExecutor =
            new UnleashScheduledExecutorImpl();
    private int periodicalTaskCounter;

    @BeforeEach
    public void setup() {
        this.periodicalTaskCounter = 0;
    }

    @Test
    public void scheduleOnce_doNotInterfereWithPeriodicalTasks() {
        unleashScheduledExecutor.setInterval(this::periodicalTask, 0, 1);
        unleashScheduledExecutor.scheduleOnce(this::sleep5seconds);
        sleep5seconds();
        assertThat(periodicalTaskCounter).isGreaterThan(3);
    }

    private void sleep5seconds() {
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void periodicalTask() {
        this.periodicalTaskCounter++;
    }

    @Test
    public void shutdown_stopsRunningScheduledTasks() {
        unleashScheduledExecutor.setInterval(this::periodicalTask, 5, 1);
        unleashScheduledExecutor.shutdown();
        sleep5seconds();
        assertThat(periodicalTaskCounter).isEqualTo(0);
    }
}
