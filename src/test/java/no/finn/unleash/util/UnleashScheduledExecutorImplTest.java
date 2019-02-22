package no.finn.unleash.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UnleashScheduledExecutorImplTest {

    private UnleashScheduledExecutorImpl unleashScheduledExecutor = new UnleashScheduledExecutorImpl();
    private int periodicalTaskCounter;

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

}