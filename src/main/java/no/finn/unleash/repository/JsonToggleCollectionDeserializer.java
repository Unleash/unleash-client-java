package no.finn.unleash.repository;

import java.lang.reflect.Type;
import java.util.Collection;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import no.finn.unleash.FeatureToggle;

public class JsonToggleCollectionDeserializer implements JsonDeserializer<ToggleCollection> {

    @Override
    public ToggleCollection deserialize(
            JsonElement jsonElement,
            Type type,
            JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
        if(!jsonElement.getAsJsonObject().has("features")) {
            return null;
        }

        JsonElement features = jsonElement.getAsJsonObject().get("features");

        if(!jsonElement.getAsJsonObject().has("version")) {
            //TODO; do not mutate, but return a new list of features instead!
            fixFormat(jsonElement);
        }

        Type collectionType = new TypeToken<Collection<FeatureToggle>>() {}.getType();
        Collection<FeatureToggle> featureToggles = jsonDeserializationContext.deserialize(features, collectionType);
        ToggleCollection collection = new ToggleCollection(featureToggles);
        return collection;
    }

    private void fixFormat(JsonElement jsonElement) {
        if(!jsonElement.getAsJsonObject().has("features")) {
            return;
        }

        JsonArray features = jsonElement.getAsJsonObject().getAsJsonArray("features");
        jsonElement.getAsJsonObject().add("version", new JsonPrimitive(1));

        features.forEach(elm -> {
            JsonObject feature = elm.getAsJsonObject();
            JsonObject strategy = new JsonObject();

            strategy.add("name", feature.get("strategy"));
            strategy.add("parameters", feature.getAsJsonObject().get("parameters"));

            JsonArray strategies = new JsonArray();
            strategies.add(strategy);

            feature.add("strategies", strategies);
            feature.remove("strategy");
            feature.remove("parameters");
        });

    }
}