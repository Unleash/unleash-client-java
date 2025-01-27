package io.getunleash.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import io.getunleash.FeatureDefinition;
import java.lang.reflect.Type;
import java.util.List;

public class ClientFeaturesParser {

    private static Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(FeatureDefinition.class, new FeatureDefinitionAdapter())
                    .create();

    public static List<FeatureDefinition> parse(String clientFeatures) {
        JsonObject jsonObject = gson.fromJson(clientFeatures, JsonObject.class);

        JsonArray featuresArray = jsonObject.getAsJsonArray("features");

        Type listType = new TypeToken<List<FeatureDefinition>>() {}.getType();
        return gson.fromJson(featuresArray, listType);
    }
}
