package io.getunleash.util;

import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DateTimeSerializer implements JsonSerializer<LocalDateTime> {
    @Override
    public JsonElement serialize(
            LocalDateTime localDateTime,
            Type type,
            JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(ISO_INSTANT.format(localDateTime.toInstant(ZoneOffset.UTC)));
    }
}
