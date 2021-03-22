package no.finn.unleash.repository;

public interface ToggleBootstrapProvider {
    /**
     * Should return JSON string parseable to /api/client/features format Look in
     * src/test/resources/features-v1.json or src/test/resources/unleash-repo-v1.json for example
     * Example in {@link no.finn.unleash.repository.ToggleBootstrapFileProvider}
     *
     * @return JSON string that can be sent to {@link
     *     no.finn.unleash.repository.ToggleBootstrapHandler#parse(String)}
     */
    String read();
}
