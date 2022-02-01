package io.getunleash.repository;

import com.google.gson.JsonSyntaxException;
import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.lang.Nullable;
import io.getunleash.util.UnleashConfig;
import java.io.StringReader;
import java.util.Collections;

public class ToggleBootstrapHandler {
    private final EventDispatcher eventDispatcher;
    private final ToggleBootstrapProvider toggleBootstrapProvider;

    public ToggleBootstrapHandler(UnleashConfig unleashConfig) {
        if (unleashConfig.getToggleBootstrapProvider() != null) {
            this.toggleBootstrapProvider = unleashConfig.getToggleBootstrapProvider();
        } else {
            this.toggleBootstrapProvider = new ToggleBootstrapFileProvider();
        }
        this.eventDispatcher = new EventDispatcher(unleashConfig);
    }

    public ToggleCollection parse(@Nullable String jsonString) {
        if (jsonString != null) {
            try (StringReader stringReader = new StringReader(jsonString)) {
                ToggleCollection toggleCollection = JsonToggleParser.fromJson(stringReader);
                eventDispatcher.dispatch(new ToggleBootstrapRead(toggleCollection));
                return toggleCollection;
            } catch (IllegalStateException | JsonSyntaxException ise) {
                eventDispatcher.dispatch(
                        new UnleashException("Failed to read toggle bootstrap", ise));
            }
        }
        return new ToggleCollection(Collections.emptyList());
    }

    public ToggleCollection read() {
        if (toggleBootstrapProvider != null) {
            return parse(toggleBootstrapProvider.read());
        }
        return new ToggleCollection(Collections.emptyList());
    }

    public static class ToggleBootstrapRead implements UnleashEvent {
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
