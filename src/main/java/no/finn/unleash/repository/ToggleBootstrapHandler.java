package no.finn.unleash.repository;

import java.util.Optional;

public interface ToggleBootstrapHandler {
    ToggleCollection readAndValidate();

    boolean validate(String expectedSha, Optional<String> calculatedSha);
}
