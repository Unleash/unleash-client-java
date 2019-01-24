package no.finn.unleash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.metric.UnleashMetricService;
import no.finn.unleash.metric.UnleashMetricServiceImpl;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.ToggleBackupHandlerFile;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleRepository;
import no.finn.unleash.strategy.ApplicationHostnameStrategy;
import no.finn.unleash.strategy.DefaultStrategy;
import no.finn.unleash.strategy.GradualRolloutRandomStrategy;
import no.finn.unleash.strategy.GradualRolloutSessionIdStrategy;
import no.finn.unleash.strategy.GradualRolloutUserIdStrategy;
import no.finn.unleash.strategy.RemoteAddressStrategy;
import no.finn.unleash.strategy.Strategy;
import no.finn.unleash.strategy.StrategyUtils;
import no.finn.unleash.strategy.UnknownStrategy;
import no.finn.unleash.strategy.UserWithIdStrategy;
import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import no.finn.unleash.util.UnleashScheduledExecutorImpl;
import no.finn.unleash.strategy.Variant;

public final class DefaultUnleash implements Unleash {
    private static final List<Strategy> BUILTIN_STRATEGIES = Arrays.asList(new DefaultStrategy(),
            new ApplicationHostnameStrategy(),
            new GradualRolloutRandomStrategy(),
            new GradualRolloutSessionIdStrategy(),
            new GradualRolloutUserIdStrategy(),
            new RemoteAddressStrategy(),
            new UserWithIdStrategy());

    private static final UnknownStrategy UNKNOWN_STRATEGY = new UnknownStrategy();
    private static final UnleashScheduledExecutor unleashScheduledExecutor = new UnleashScheduledExecutorImpl();

    private final UnleashMetricService metricService;
    private final ToggleRepository toggleRepository;
    private final Map<String, Strategy> strategyMap;
    private final UnleashContextProvider contextProvider;


    private static FeatureToggleRepository defaultToggleRepository(UnleashConfig unleashConfig) {
        return new FeatureToggleRepository(
                unleashConfig,
                unleashScheduledExecutor,
                new HttpToggleFetcher(unleashConfig),
                new ToggleBackupHandlerFile(unleashConfig));
    }

    public DefaultUnleash(UnleashConfig unleashConfig, Strategy... strategies) {
        this(unleashConfig, defaultToggleRepository(unleashConfig), strategies);
    }

    public DefaultUnleash(UnleashConfig unleashConfig, ToggleRepository toggleRepository, Strategy... strategies) {
        this.toggleRepository = toggleRepository;
        this.strategyMap = buildStrategyMap(strategies);
        this.contextProvider = unleashConfig.getContextProvider();
        this.metricService = new UnleashMetricServiceImpl(unleashConfig, unleashScheduledExecutor);
        metricService.register(strategyMap.keySet());
    }

    @Override
    public boolean isEnabled(final String toggleName) {
        return isEnabled(toggleName, false);
    }

    @Override
    public boolean isEnabled(final String toggleName, final boolean defaultSetting) {
        return isEnabled(toggleName, contextProvider.getContext(), defaultSetting);
    }

    @Override
    public boolean isEnabled(final String toggleName, final UnleashContext context ,final boolean defaultSetting) {
        FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        return isEnabled(toggleName, context, defaultSetting, featureToggle);
    }

    private boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting, FeatureToggle featureToggle) {
        boolean enabled;
        if (featureToggle == null) {
            enabled = defaultSetting;
        } else if(!featureToggle.isEnabled()) {
            enabled = false;
        } else if(featureToggle.getStrategies().size() == 0) {
            return true;
        } else {
            enabled = featureToggle.getStrategies().stream()
                    .filter(as -> getStrategy(as.getName()).isEnabled(as.getParameters(), context))
                    .findFirst()
                    .isPresent();
        }

        count(toggleName, enabled);
        return enabled;
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, context, "");
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, String defaultPayload) {
        final  FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        final boolean isEnabled = isEnabled(toggleName, context, false, featureToggle);

        if(isEnabled) {
            return selectVariant(featureToggle, context)
                .map(variantDefinition -> new Variant(variantDefinition.getName(), variantDefinition.getPayload(), isEnabled))
                .orElseGet(() -> new Variant("default", defaultPayload, isEnabled));
        } else {
            return new Variant("default", defaultPayload, isEnabled);
        }
    }

    private Optional<VariantDefinition> selectVariant(final FeatureToggle featureToggle, UnleashContext context) {
        if(featureToggle.getVariants() == null || featureToggle.getVariants().isEmpty()){
            return Optional.empty();
        }

        final int sum = featureToggle.getVariants().stream().mapToInt(VariantDefinition::getWeight).sum();
        final int score = 1 + StrategyUtils.getNormalizedNumber(
            context.getUserId()
                .orElse(context.getSessionId()
                    .orElse(context.getRemoteAddress()
                        .orElse(""))),
            featureToggle.getName(),
            sum);

        int num = 0;
        for (VariantDefinition definition: featureToggle.getVariants()){
            if(score + num >= definition.getWeight()){
                return Optional.of(definition);
            }
            num += definition.getWeight();
        }

        //Should not happen
        return Optional.of(featureToggle.getVariants().get(featureToggle.getVariants().size() - 1));
    }

    public Optional<FeatureToggle> getFeatureToggleDefinition(String toggleName) {
        return Optional.ofNullable(toggleRepository.getToggle(toggleName));
    }

    public List<String> getFeatureToggleNames() {
        return toggleRepository.getFeatureNames();
    }

    public void count(final String toggleName, boolean enabled) {
        metricService.count(toggleName, enabled);
    }

    private Map<String, Strategy> buildStrategyMap(Strategy[] strategies) {
        Map<String, Strategy> map = new HashMap<>();

        BUILTIN_STRATEGIES.forEach(strategy -> map.put(strategy.getName(), strategy));

        if (strategies != null) {
            for (Strategy strategy : strategies) {
                map.put(strategy.getName(), strategy);
            }
        }

        return map;
    }



    private Strategy getStrategy(String strategy) {
        return strategyMap.containsKey(strategy) ? strategyMap.get(strategy) : UNKNOWN_STRATEGY;
    }
}
