package io.getunleash.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import io.getunleash.FeatureToggle;
import io.getunleash.Segment;

import java.lang.reflect.Type;
import java.util.Collection;

public class JsonFeatureSerializer implements JsonSerializer<FeatureCollection> {

    private static final Type SEGMENT_COLLECTION_TYPE =
        new TypeToken<Collection<Segment>>() {}.getType();
    private static final Type TOGGLE_COLLECTION_TYPE =
        new TypeToken<Collection<FeatureToggle>>() {}.getType();

    @Override
    public JsonElement serialize(FeatureCollection featureCollection, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject json = new JsonObject();

        json.addProperty("version", 2);
        json.add("features", jsonSerializationContext.serialize(featureCollection.getToggleCollection().getFeatures(), TOGGLE_COLLECTION_TYPE).getAsJsonArray());
        json.add("segments", jsonSerializationContext.serialize(featureCollection.getSegmentCollection().getSegments(), SEGMENT_COLLECTION_TYPE).getAsJsonArray());

        return json;
    }
}
