package no.finn.unleash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import no.finn.unleash.metric.UnleashMetricService;
import no.finn.unleash.metric.UnleashMetricServiceImpl;
import no.finn.unleash.repository.FeatureToggleRepository;
import no.finn.unleash.repository.HttpToggleFetcher;
import no.finn.unleash.repository.ToggleBackupHandlerFile;
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
import no.finn.unleash.strategy.Variant;
import no.finn.unleash.util.UnleashConfig;
import no.finn.unleash.util.UnleashScheduledExecutor;
import no.finn.unleash.util.UnleashScheduledExecutorImpl;

import static java.util.Optional.ofNullable;

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
    private static final Variant defaultVariant = new Variant("disabled", null, false);


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
        return isEnabled(toggleName, context, defaultSetting, featureToggle, true);
    }

    private boolean isEnabled(String toggleName, UnleashContext context, boolean defaultSetting, FeatureToggle featureToggle, boolean count) {
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

        if(count) {
            count(toggleName, enabled);
        }
        return enabled;
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context) {
        return getVariant(toggleName, context, null);
    }

    @Override
    public Variant getVariant(String toggleName, UnleashContext context, Variant defaultValue) {
        final FeatureToggle featureToggle = toggleRepository.getToggle(toggleName);
        return Optional.of(isEnabled(toggleName, context, false, featureToggle, false))
            .filter(aBoolean -> aBoolean)
            .flatMap(isEnabled ->
                selectVariant(featureToggle, context)
                    .map(variantDefinition -> getVariant(isEnabled, variantDefinition, toggleName))
            ).orElseGet(() -> ofNullable(defaultValue).orElse(defaultVariant));
    }

    @Override
    public Variant getVariant(String toggleName) {
        return getVariant(toggleName, contextProvider.getContext());
    }

    @Override
    public Variant getVariant(String toggleName, Variant defaultValue) {
        return getVariant(toggleName, contextProvider.getContext(), defaultValue);
    }

    private Variant getVariant(boolean isEnabled, VariantDefinition variantDefinition, final String toggleName) {
        metricService.countVariant(toggleName, variantDefinition.getName());
        return new Variant(variantDefinition.getName(), variantDefinition.getPayload(), isEnabled);
    }

    private Optional<VariantDefinition> selectVariant(final FeatureToggle featureToggle, UnleashContext context) {
        if (featureToggle.getVariants() == null || featureToggle.getVariants().isEmpty()) {
            return Optional.empty();
        }

        final int sum = featureToggle.getVariants().stream().mapToInt(VariantDefinition::getWeight).sum();
        final int score = 1 + StrategyUtils.getNormalizedNumber(
            context.getUserId()
                .orElse(context.getSessionId()
                    .orElse(context.getRemoteAddress()
                        .orElse(Double.toString(Math.random())))),
            featureToggle.getName(),
            sum);

        int num = 0;
        for (final VariantDefinition definition : featureToggle.getVariants()) {
            if (definition.getWeight() != 0) {
                if (score + num >= definition.getWeight()) {
                    return Optional.of(definition);
                }
                num += definition.getWeight();
            }
        }

        //Should not happen
        return Optional.empty();
    }

    public Optional<FeatureToggle> getFeatureToggleDefinition(String toggleName) {
        return ofNullable(toggleRepository.getToggle(toggleName));
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
