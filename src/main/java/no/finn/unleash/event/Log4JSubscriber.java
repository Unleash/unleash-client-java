package no.finn.unleash.event;

import no.finn.unleash.UnleashException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4JSubscriber implements UnleashSubscriber {

    private static final Logger LOG = LogManager.getLogger(Log4JSubscriber.class);

    private Level eventLevel = Level.INFO;
    private Level errorLevel = Level.WARN;

    @Override
    public void on(UnleashEvent unleashEvent) {
        LOG.log(eventLevel, unleashEvent.toString());
    }

    @Override
    public void onError(UnleashException unleashException) {
        LOG.log(errorLevel, unleashException.getMessage(), unleashException);
    }

    public Log4JSubscriber setEventLevel(Level eventLevel) {
        this.eventLevel = eventLevel;
        return this;
    }

    public Log4JSubscriber setErrorLevel(Level errorLevel) {
        this.errorLevel = errorLevel;
        return this;
    }

}
