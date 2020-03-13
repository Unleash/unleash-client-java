package no.finn.unleash;

import java.util.HashMap;
import java.util.Map;

public class DefaultCustomHttpHeadersProviderImpl implements CustomHttpHeadersProvider {
    @Override
    public Map<String, String> getCustomHeaders() {
        return new HashMap<>();
    }
}
