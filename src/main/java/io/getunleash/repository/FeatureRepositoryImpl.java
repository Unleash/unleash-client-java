package io.getunleash.repository;

import io.getunleash.FeatureDefinition;
import io.getunleash.UnleashContext;
import io.getunleash.UnleashException;
import io.getunleash.engine.UnleashEngine;
import io.getunleash.engine.VariantDef;
import io.getunleash.engine.WasmResponse;
import io.getunleash.engine.YggdrasilInvalidInputException;
import io.getunleash.event.ClientFeaturesResponse;
import io.getunleash.event.EventDispatcher;
import io.getunleash.event.UnleashReady;
import io.getunleash.util.Throttler;
import io.getunleash.util.UnleashConfig;
import io.getunleash.util.UnleashScheduledExecutor;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureRepositoryImpl implements FeatureRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureRepositoryImpl.class);
    private final UnleashConfig unleashConfig;
    private final BackupHandler featureBackupHandler;
    private final ToggleBootstrapProvider bootstrapper;
    private final FeatureFetcher featureFetcher;
    private final EventDispatcher eventDispatcher;
    private final UnleashEngine engine;
    private final Throttler throttler;
    private boolean ready;

    public FeatureRepositoryImpl(UnleashConfig unleashConfig, UnleashEngine engine) {
        this(unleashConfig, new FeatureBackupHandlerFile(unleashConfig), engine);
    }

    public FeatureRepositoryImpl(
            UnleashConfig unleashConfig, BackupHandler featureBackupHandler, UnleashEngine engine) {
        this(
                unleashConfig,
                featureBackupHandler,
                engine,
                unleashConfig.getUnleashFeatureFetcherFactory().apply(unleashConfig));
    }

    public FeatureRepositoryImpl(
            UnleashConfig unleashConfig,
            BackupHandler featureBackupHandler,
            UnleashEngine engine,
            FeatureFetcher fetcher) {
        this(
                unleashConfig,
                featureBackupHandler,
                engine,
                fetcher,
                unleashConfig.getToggleBootstrapProvider());
    }

    public FeatureRepositoryImpl(
            UnleashConfig unleashConfig,
            BackupHandler featureBackupHandler,
            UnleashEngine engine,
            FeatureFetcher fetcher,
            ToggleBootstrapProvider bootstrapHandler) {
        this(
                unleashConfig,
                featureBackupHandler,
                engine,
                fetcher,
                bootstrapHandler,
                new EventDispatcher(unleashConfig));
    }

    public FeatureRepositoryImpl(
            UnleashConfig unleashConfig,
            BackupHandler featureBackupHandler,
            UnleashEngine engine,
            FeatureFetcher fetcher,
            ToggleBootstrapProvider bootstrapHandler,
            EventDispatcher eventDispatcher) {
        this.unleashConfig = unleashConfig;
        this.featureBackupHandler = featureBackupHandler;
        this.engine = engine;
        this.featureFetcher = fetcher;
        this.bootstrapper = bootstrapHandler;
        this.eventDispatcher = eventDispatcher;
        this.throttler =
                new Throttler(
                        (int) unleashConfig.getFetchTogglesInterval(),
                        300,
                        unleashConfig.getUnleashURLs().getFetchTogglesURL());
        this.initCollections(unleashConfig.getScheduledExecutor());
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    private void initCollections(UnleashScheduledExecutor executor) {
        Optional<String> features = this.featureBackupHandler.read();
        if (!features.isPresent() && this.bootstrapper != null) {
            features = this.bootstrapper.read();
        }
        if (features.isPresent()) {
            try {
                this.engine.takeState(features.get());
            } catch (YggdrasilInvalidInputException e) {
                LOGGER.error("Error when initializing feature toggles", e);
                eventDispatcher.dispatch(new UnleashException("Failed to read backup file:", e));
            }
        }

        if (unleashConfig.isSynchronousFetchOnInitialisation()) {
            if (this.unleashConfig.getStartupExceptionHandler() != null) {
                updateFeatures(this.unleashConfig.getStartupExceptionHandler()).run();
            } else {
                updateFeatures(
                                e -> {
                                    throw e;
                                }) // just throw exception handler
                        .run();
            }
        }

        if (!unleashConfig.isDisablePolling()) {
            Runnable updateFeatures = updateFeatures(this.eventDispatcher::dispatch);
            if (unleashConfig.getFetchTogglesInterval() > 0) {
                executor.setInterval(updateFeatures, 0, unleashConfig.getFetchTogglesInterval());
            } else {
                executor.scheduleOnce(updateFeatures);
            }
        }
    }

    private Runnable updateFeatures(final Consumer<UnleashException> handler) {
        return () -> {
            if (throttler.performAction()) {
                try {
                    ClientFeaturesResponse response = featureFetcher.fetchFeatures();
                    eventDispatcher.dispatch(response);
                    if (response.getStatus() == ClientFeaturesResponse.Status.CHANGED) {
                        String clientFeatures = response.getClientFeatures().get();

                        this.engine.takeState(clientFeatures);
                        featureBackupHandler.write(clientFeatures);
                    } else if (response.getStatus() == ClientFeaturesResponse.Status.UNAVAILABLE) {
                        if (!ready && unleashConfig.isSynchronousFetchOnInitialisation()) {
                            throw new UnleashException(
                                    String.format(
                                            "Could not initialize Unleash, got response code %d",
                                            response.getHttpStatusCode()),
                                    null);
                        }
                        if (ready) {
                            throttler.handleHttpErrorCodes(response.getHttpStatusCode());
                        }
                        return;
                    }
                    throttler.decrementFailureCountAndResetSkips();
                    if (!ready) {
                        eventDispatcher.dispatch(new UnleashReady());
                        ready = true;
                    }
                } catch (UnleashException e) {
                    handler.accept(e);
                } catch (YggdrasilInvalidInputException e) {
                    handler.accept(new UnleashException("Error when fetching features", e));
                }
            } else {
                throttler.skipped(); // We didn't do anything this iteration, just reduce the count
            }
        };
    }

    public Integer getFailures() {
        return this.throttler.getFailures();
    }

    public Integer getSkips() {
        return this.throttler.getSkips();
    }

    @Override
    public WasmResponse<Boolean> isEnabled(String toggleName, UnleashContext context) {
        try {
            return this.engine.isEnabled(toggleName, YggdrasilAdapters.adapt(context));
        } catch (YggdrasilInvalidInputException e) {
            LOGGER.error("Error when checking feature toggle {}", toggleName, e);
            return null;
        }
    }

    @Override
    public WasmResponse<VariantDef> getVariant(String toggleName, UnleashContext context) {
        try {
            return this.engine.getVariant(toggleName, YggdrasilAdapters.adapt(context));
        } catch (YggdrasilInvalidInputException e) {
            LOGGER.error("Error when checking feature toggle {}", toggleName, e);
            return null;
        }
    }

    @Override
    public Stream<FeatureDefinition> listKnownToggles() {
        return this.engine.listKnownToggles().stream().map(FeatureDefinition::new);
    }
}
