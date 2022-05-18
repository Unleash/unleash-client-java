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

public class FeatureBootstrapHandler {
    private final EventDispatcher eventDispatcher;
    private final ToggleBootstrapProvider toggleBootstrapProvider;

    public FeatureBootstrapHandler(UnleashConfig unleashConfig) {
        if (unleashConfig.getToggleBootstrapProvider() != null) {
            this.toggleBootstrapProvider = unleashConfig.getToggleBootstrapProvider();
        } else {
            this.toggleBootstrapProvider = new ToggleBootstrapFileProvider();
        }
        this.eventDispatcher = new EventDispatcher(unleashConfig);
    }

    public FeatureCollection parse(@Nullable String jsonString) {
        if (jsonString != null) {
            try (StringReader stringReader = new StringReader(jsonString)) {
                FeatureCollection featureCollection = JsonFeatureParser.fromJson(stringReader);
                eventDispatcher.dispatch(new FeatureBootstrapRead(featureCollection));
                return featureCollection;
            } catch (IllegalStateException | JsonSyntaxException ise) {
                eventDispatcher.dispatch(
                        new UnleashException("Failed to read toggle bootstrap", ise));
            }
        }
        return new FeatureCollection(
                new ToggleCollection(Collections.emptyList()),
                new SegmentCollection(Collections.emptyList()));
    }

    public FeatureCollection read() {
        if (toggleBootstrapProvider != null) {
            return parse(toggleBootstrapProvider.read());
        }
        return new FeatureCollection(
                new ToggleCollection(Collections.emptyList()),
                new SegmentCollection(Collections.emptyList()));
    }

    public static class FeatureBootstrapRead implements UnleashEvent {
        private final FeatureCollection featureCollection;

        public FeatureBootstrapRead(FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.featuresBootstrapped(featureCollection);
        }
    }
}
