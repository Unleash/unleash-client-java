package io.getunleash;

import java.util.Map;

public interface CustomHttpHeadersProvider {
    Map<String, String> getCustomHeaders();
}
