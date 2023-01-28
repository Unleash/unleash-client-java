package io.getunleash.repository;

import io.getunleash.UnleashException;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashEvent;
import io.getunleash.event.UnleashSubscriber;
import io.getunleash.util.UnleashConfig;

import java.util.Collections;

public abstract class AbstractBackupHandler implements BackupHandler<FeatureCollection> {
    private final EventDispatcher eventDispatcher;

    public AbstractBackupHandler(UnleashConfig config) {
        this.eventDispatcher = new EventDispatcher(config);
    }

    @Override
    public FeatureCollection read() {
        try {
            FeatureCollection collection = readFeatureCollection();

            eventDispatcher.dispatch(new FeatureBackupRead(collection));

            return collection;
        } catch (UnleashException ex) {
            eventDispatcher.dispatch(ex);
        }

        return new FeatureCollection(
            new ToggleCollection(Collections.emptyList()),
            new SegmentCollection(Collections.emptyList()));
    }

    @Override
    public void write(FeatureCollection collection) {
        try {
            writeFeatureCollection(collection);

            eventDispatcher.dispatch(new FeatureBackupWritten(collection));
        } catch (UnleashException ex) {
            eventDispatcher.dispatch(ex);
        }
    }

    protected abstract FeatureCollection readFeatureCollection();

    protected abstract void writeFeatureCollection(FeatureCollection featureCollection);


    private static class FeatureBackupRead implements UnleashEvent {

        private final FeatureCollection featureCollection;

        private FeatureBackupRead(FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.featuresBackupRestored(featureCollection);
        }
    }

    private static class FeatureBackupWritten implements UnleashEvent {

        private final FeatureCollection featureCollection;

        private FeatureBackupWritten(FeatureCollection featureCollection) {
            this.featureCollection = featureCollection;
        }

        @Override
        public void publishTo(UnleashSubscriber unleashSubscriber) {
            unleashSubscriber.featuresBackedUp(featureCollection);
        }
    }
}
