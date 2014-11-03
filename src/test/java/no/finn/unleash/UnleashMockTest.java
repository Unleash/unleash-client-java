package no.finntech.unleash;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UnleashMockTest {

    @Test
    public void should_enable_all_toggles() throws Exception {
        UnleashMock unleashMock = new UnleashMock();
        unleashMock.enableAll();

        assertThat(unleashMock.isEnabled("unknown"), is(true));
        assertThat(unleashMock.isEnabled("unknown2"), is(true));
    }

    @Test
    public void should_enable_specific_toggles() throws Exception {
        UnleashMock unleashMock = new UnleashMock();
        unleashMock.enable("t1", "t2");

        assertThat(unleashMock.isEnabled("t1"), is(true));
        assertThat(unleashMock.isEnabled("t2"), is(true));
        assertThat(unleashMock.isEnabled("unknown"), is(false));
    }

    @Test
    public void should_disable_all_toggles() {
        UnleashMock unleashMock = new UnleashMock();
        unleashMock.enable("t1", "t2");
        unleashMock.disableAll();

        assertThat(unleashMock.isEnabled("t1"), is(false));
    }

    @Test
    public void should_disable_one_toggle() {
        UnleashMock unleashMock = new UnleashMock();
        unleashMock.enable("t1", "t2");
        unleashMock.disable("t2");

        assertThat(unleashMock.isEnabled("t1"), is(true));
        assertThat(unleashMock.isEnabled("t2"), is(false));
    }
}