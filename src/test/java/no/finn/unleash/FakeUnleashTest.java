package no.finn.unleash;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FakeUnleashTest {

    @Test
    public void should_enable_all_toggles() throws Exception {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enableAll();

        assertThat(fakeUnleash.isEnabled("unknown"), is(true));
        assertThat(fakeUnleash.isEnabled("unknown2"), is(true));
    }

    @Test
    public void should_enable_specific_toggles() throws Exception {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable("t1", "t2");

        assertThat(fakeUnleash.isEnabled("t1"), is(true));
        assertThat(fakeUnleash.isEnabled("t2"), is(true));
        assertThat(fakeUnleash.isEnabled("unknown"), is(false));
    }

    @Test
    public void should_disable_all_toggles() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable("t1", "t2");
        fakeUnleash.disableAll();

        assertThat(fakeUnleash.isEnabled("t1"), is(false));
    }

    @Test
    public void should_disable_one_toggle() {
        FakeUnleash fakeUnleash = new FakeUnleash();
        fakeUnleash.enable("t1", "t2");
        fakeUnleash.disable("t2");

        assertThat(fakeUnleash.isEnabled("t1"), is(true));
        assertThat(fakeUnleash.isEnabled("t2"), is(false));
    }
}