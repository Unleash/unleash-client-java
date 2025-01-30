package io.getunleash.repository;

import java.util.Optional;

public interface ToggleBootstrapProvider {
    /**
     * Should return JSON string parsable to /api/client/features format Look in
     * src/test/resources/features-v1.json or src/test/resources/unleash-repo-v1.json for example
     * Example in {@link ToggleBootstrapFileProvider}
     *
     * @return JSON string representing a response from /api/client/features
     */
    Optional<String> read();
}
