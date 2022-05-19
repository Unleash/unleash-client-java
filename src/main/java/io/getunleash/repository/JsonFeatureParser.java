package io.getunleash.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;

final class JsonFeatureParser {

    private JsonFeatureParser() {}

    public static String toJsonString(FeatureCollection featureCollection) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(featureCollection);
    }

    public static FeatureCollection fromJson(Reader reader) throws IllegalStateException {
        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(
                                FeatureCollection.class, new JsonFeaturesDeserializer())
                        .create();
        FeatureCollection gsonCollection = gson.fromJson(reader, FeatureCollection.class);
        if (gsonCollection == null) {
            throw new IllegalStateException("Could not extract features from json");
        }
        return new FeatureCollection(
                gsonCollection.getToggleCollection(), gsonCollection.getSegmentCollection());
    }
}
