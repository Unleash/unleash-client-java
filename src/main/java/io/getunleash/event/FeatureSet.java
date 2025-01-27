package io.getunleash.event;

import io.getunleash.FeatureDefinition;
import io.getunleash.util.ClientFeaturesParser;
import java.util.List;

public class FeatureSet implements UnleashEvent {

    private final String clientFeatures;
    private List<FeatureDefinition> features;

    public FeatureSet(String clientFeatures) {
        this.clientFeatures = clientFeatures;
    }

    public List<FeatureDefinition> getFeatures() {
        if (features == null) {
            features = ClientFeaturesParser.parse(clientFeatures);
        }
        return features;
    }

    @Override
    public void publishTo(UnleashSubscriber unleashSubscriber) {
        unleashSubscriber.on(this);
    }
}
