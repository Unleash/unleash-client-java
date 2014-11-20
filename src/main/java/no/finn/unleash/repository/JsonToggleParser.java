package no.finn.unleash.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.util.Collection;
import no.finn.unleash.FeatureToggle;

final class JsonToggleParser {

    private JsonToggleParser() {
    }

    public static String toJsonString(ToggleCollection toggleCollection) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(toggleCollection);
    }

    public static Collection<FeatureToggle> fromJson(String jsonString) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonString, ToggleCollection.class).getFeatures();
    }

    public static ToggleCollection fromJson(Reader reader) throws NullPointerException {
        Gson gson = new GsonBuilder().create();
        ToggleCollection gsonCollection = gson.fromJson(reader, ToggleCollection.class);
        if(gsonCollection == null) {
            throw new NullPointerException("Could not extract toggles from json");
        }
        return new ToggleCollection(gsonCollection.getFeatures());
    }
}
