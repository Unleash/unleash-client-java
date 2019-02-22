package no.finn.unleash.event;

public class ToggleEvaluated implements UnleashEvent {

    private final String toggleName;
    private final boolean enabled;

    public ToggleEvaluated(String toggleName, boolean enabled) {
        this.toggleName = toggleName;
        this.enabled = enabled;
    }

    public String getToggleName() {
        return toggleName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.toggleEvaluated(this);
    }

    @Override
    public String toString() {
        return "ToggleEvaluated: " + toggleName + "=" + enabled;
    }

}
