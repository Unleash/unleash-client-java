package no.finn.unleash;

import java.util.Map;

public interface CustomHttpHeadersProvider {
    Map<String, String> getCustomHeaders();
}
