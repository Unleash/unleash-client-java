package io.getunleash.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;

public final class JsonFeatureParser {

    private JsonFeatureParser() {}

    public static String toJsonString(FeatureCollection featureCollection) {
        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(FeatureCollection.class, new JsonFeatureSerializer())
                        .create();
        return gson.toJson(featureCollection);
    }

    public static FeatureCollection fromJson(Reader reader) throws IllegalStateException {
        Gson gson =
                new GsonBuilder()
                        .registerTypeAdapter(
                                FeatureCollection.class, new JsonFeaturesDeserializer())
                        .create();
        FeatureCollection featureCollection = gson.fromJson(reader, FeatureCollection.class);
        if (featureCollection == null) {
            throw new IllegalStateException("Could not extract features from json");
        }
        return featureCollection;
    }
}
