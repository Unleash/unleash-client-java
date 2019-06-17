package no.finn.unleash.event;

import no.finn.unleash.UnleashException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jSubscriber implements UnleashSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(Slf4jSubscriber.class);

    @Override
    public void on(UnleashEvent unleashEvent) {
        LOG.info(unleashEvent.toString());
    }

    @Override
    public void onError(UnleashException unleashException) {
        LOG.error(unleashException.getMessage(), unleashException);
    }
}
