package io.getunleash;

import io.getunleash.repository.FeatureRepository;
import io.getunleash.strategy.Strategy;
import io.getunleash.util.UnleashConfig;
import java.util.HashMap;
import java.util.Map;

public class UnleashClientFactory {

    private static UnleashClientFactory INSTANCE;

    private static Map<String, DefaultUnleash> unleashClients = new HashMap<>();

    private UnleashClientFactory() {}

    public static synchronized UnleashClientFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UnleashClientFactory();
        }

        return INSTANCE;
    }

    public synchronized Unleash getClient(UnleashConfig config, Strategy... strategies) {
        return getClient(config, DefaultUnleash.defaultToggleRepository(config), strategies);
    }

    public synchronized Unleash getClient(
            UnleashConfig config, FeatureRepository featureRepository, Strategy... strategies) {
        String key = config.getUnleashAPI() + config.getAppName();
        if (unleashClients.containsKey(key)) {
            System.out.println("Returning existing client");
            return unleashClients.get(key);
        }
        DefaultUnleash unleash = new DefaultUnleash(config, featureRepository, strategies);
        unleashClients.put(key, unleash);
        return unleash;
    }
}
