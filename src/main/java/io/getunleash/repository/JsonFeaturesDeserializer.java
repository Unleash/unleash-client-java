package io.getunleash.repository;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.getunleash.FeatureToggle;
import io.getunleash.Segment;
import io.getunleash.lang.Nullable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class JsonFeaturesDeserializer implements JsonDeserializer<FeatureCollection> {
    private static final Type PARAMS_TYPE = new TypeToken<Map<String, String>>() {}.getType();

    private static final Type SEGMENT_COLLECTION_TYPE =
            new TypeToken<Collection<Segment>>() {}.getType();
    private static final Type TOGGLE_COLLECTION_TYPE =
            new TypeToken<Collection<FeatureToggle>>() {}.getType();

    @Override
    public @Nullable FeatureCollection deserialize(
            JsonElement rootElement, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        return deserializeVersion(rootElement, context);
    }

    static @Nullable FeatureCollection deserializeVersion(
            JsonElement rootElement, JsonDeserializationContext context) {
        if (!rootElement.getAsJsonObject().has("features")) {
            return null;
        }
        JsonObject root = rootElement.getAsJsonObject();
        JsonArray segmentArray = root.getAsJsonArray("segments");
        JsonArray togglesArray = root.getAsJsonArray("features");

        Collection<FeatureToggle> toggles =
                context.deserialize(togglesArray, TOGGLE_COLLECTION_TYPE);
        Collection<Segment> segments = context.deserialize(segmentArray, SEGMENT_COLLECTION_TYPE);
        return new FeatureCollection(
                new ToggleCollection(toggles),
                new SegmentCollection(segments));
    }
}
