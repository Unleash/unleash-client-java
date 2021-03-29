package no.finn.unleash.repository;

import java.io.StringReader;
import java.util.Collections;
import no.finn.unleash.UnleashException;
import no.finn.unleash.event.EventDispatcher;
import no.finn.unleash.event.UnleashEvent;
import no.finn.unleash.event.UnleashSubscriber;
import no.finn.unleash.lang.Nullable;
import no.finn.unleash.util.UnleashConfig;

public class ToggleBootstrapHandler {
    private final EventDispatcher eventDispatcher;
    @Nullable private final ToggleBootstrapProvider toggleBootstrapProvider;

    public ToggleBootstrapHandler(UnleashConfig unleashConfig) {
        this.toggleBootstrapProvider = unleashConfig.getToggleBootstrapProvider();
        this.eventDispatcher = new EventDispatcher(unleashConfig);
    }

    public ToggleCollection parse(String jsonString) {
        try (StringReader stringReader = new StringReader(jsonString)) {
            ToggleCollection toggleCollection = JsonToggleParser.fromJson(stringReader);
            eventDispatcher.dispatch(new ToggleBootstrapRead(toggleCollection));
            return toggleCollection;
        } catch (IllegalStateException ise) {
            eventDispatcher.dispatch(new UnleashException("Failed to read toggle bootstrap", ise));
        }
        return new ToggleCollection(Collections.emptyList());
    }

    public ToggleCollection read() {
        if (toggleBootstrapProvider != null) {
            return parse(toggleBootstrapProvider.read());
        }
        return new ToggleCollection(Collections.emptyList());
    }

    private static class ToggleBootstrapRead implements UnleashEvent {
        private final ToggleCollection toggleCollection;

        private ToggleBootstrapRead(ToggleCollection toggleCollection) {
            this.toggleCollection = toggleCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.togglesBootstrapped(toggleCollection);
        }
    }
}
