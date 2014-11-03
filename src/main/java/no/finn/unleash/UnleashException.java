package no.finn.unleash;

public class UnleashException extends RuntimeException {

    public UnleashException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnleashException(String message) {
        super(message);
    }
}
