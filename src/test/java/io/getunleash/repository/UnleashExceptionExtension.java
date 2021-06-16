package io.getunleash.repository;

import io.getunleash.UnleashException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

public class UnleashExceptionExtension implements TestExecutionExceptionHandler {
    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable)
            throws Throwable {
        if (throwable instanceof UnleashException) {
            return;
        }
        throw throwable;
    }
}
