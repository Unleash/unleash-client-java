package io.getunleash.event;

import io.getunleash.UnleashContext;
import java.util.UUID;

public class ImpressionEvent implements UnleashEvent {
    private String featureName;

    private String eventId;
    private boolean enabled;
    private UnleashContext context;

    public String getFeatureName() {
        return featureName;
    }

    public String getEventId() {
        return eventId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public UnleashContext getContext() {
        return context;
    }

    public ImpressionEvent(String featureName, boolean enabled, UnleashContext context) {
        this.featureName = featureName;
        this.enabled = enabled;
        this.eventId = UUID.randomUUID().toString();
        this.context = context;
    }

    ImpressionEvent(String featureName, String eventId, boolean enabled, UnleashContext context) {
        this.featureName = featureName;
        this.eventId = eventId;
        this.enabled = enabled;
        this.context = context;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.impression(this);
    }
}
