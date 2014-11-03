package no.finn.unleash.repository;

import no.finn.unleash.Toggle;
import no.finn.unleash.UnleashException;

import javax.json.Json;
import javax.json.stream.JsonParser;
import java.io.Reader;
import java.util.*;

import static javax.json.stream.JsonParser.Event;
import static javax.json.stream.JsonParser.Event.*;

final class UnleashParser {
    private UnleashParser() {
    }

    public static ToggleCollection from(Reader reader) {
        JsonParser parser = Json.createParser(reader);
        Collection<Toggle> toggles = Collections.emptyList();

        while (parser.hasNext()) {
            switch (parser.next()) {
                case KEY_NAME:
                    String keyName = parser.getString();
                    if ("features".equals(keyName)) {
                        toggles = parseFeatures(parser);
                    }
                    break;
                default: {
                }
            }
        }

        return new ToggleCollection(toggles);
    }

    private static Collection<Toggle> parseFeatures(JsonParser parser) {
        if (!START_ARRAY.equals(parser.next())) {
            throw new UnleashException("'features' should be an array");
        }

        Collection<Toggle> toggles = new ArrayList<>();
        String name = null;
        boolean enabled = false;
        String strategy = null;
        Map<String, String> params = Collections.emptyMap();

        while (parser.hasNext()) {
            switch (parser.next()) {
                case KEY_NAME:
                    switch (parser.getString()) {
                        case "strategy":
                            strategy = getValueString(parser);
                            break;
                        case "name":
                            name = getValueString(parser);
                            break;
                        case "enabled":
                            enabled = VALUE_TRUE.equals(parser.next());
                            break;
                        case "parameters":
                            params = parseParams(parser);
                        default: {
                        }
                    }
                    break;
                case END_OBJECT:
                    if (name != null) {
                        toggles.add(new Toggle(name, enabled, strategy, params));
                    }
                    name = null;
                    enabled = false;
                    strategy = null;
                    params = Collections.emptyMap();
                default: {
                }
            }
        }

        return toggles;
    }

    private static String getValueString(JsonParser parser) {
        Event current = parser.next();
        if (VALUE_STRING.equals(current)) {
            return parser.getString();
        }
        throw new UnleashException("Expected VALUE_STRING got " + current);
    }

    private static Map<String, String> parseParams(JsonParser parser) {
        Map<String, String> params = new HashMap<>();
        Event next = parser.next();
        assert (START_OBJECT.equals(next));
        String key = null;

        while (!END_OBJECT.equals(next)) {
            next = parser.next();
            switch (next) {
                case KEY_NAME:
                    key = parser.getString();
                    break;
                case VALUE_STRING:
                    params.put(key, parser.getString());
                    break;
                default: {
                }
            }
        }
        return params;
    }

}

