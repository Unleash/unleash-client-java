package io.getunleash.event;

import static org.slf4j.event.Level.*;

import io.getunleash.UnleashException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class Log4JSubscriber implements UnleashSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(Log4JSubscriber.class);

    private Level eventLevel = INFO;
    private Level errorLevel = WARN;

    @Override
    public void on(UnleashEvent unleashEvent) {
        switch (eventLevel) {
            case DEBUG:
                LOG.debug(unleashEvent.toString());
                break;
            case INFO:
                LOG.info(unleashEvent.toString());
                break;
            case WARN:
                LOG.warn(unleashEvent.toString());
                break;
            case ERROR:
                LOG.error(unleashEvent.toString());
                break;
            case TRACE:
                LOG.trace(unleashEvent.toString());
                break;
        }
    }

    @Override
    public void onError(UnleashException unleashException) {
        switch (errorLevel) {
            case WARN:
                LOG.warn(unleashException.getMessage(), unleashException);
                break;
            case ERROR:
                LOG.error(unleashException.getMessage(), unleashException);
                break;
            case INFO:
                LOG.info(unleashException.getMessage(), unleashException);
                break;
            case DEBUG:
                LOG.debug(unleashException.getMessage(), unleashException);
                break;
            case TRACE:
                LOG.trace(unleashException.getMessage(), unleashException);
                break;
        }
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
