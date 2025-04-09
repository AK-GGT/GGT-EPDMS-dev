package de.iai.ilcd.rest.util;

import com.google.gson.*;
import de.fzk.iai.ilcd.service.client.impl.vo.StringList;

import java.lang.reflect.Type;

/**
 * Serializer for string lists.
 */
public class JSONStringListSerializer implements JsonSerializer<StringList> {

    /**
     * Private constructor, use {@link #register(GsonBuilder)}
     */
    private JSONStringListSerializer() {
    }

    /**
     * Register serializer in provided builder. Serialization of a list
     * must be performed by passing an instance of the wrapper class {@link StringList} to {@link Gson}.
     *
     * @param gsonBuilder GSON builder
     */
    public static void register(GsonBuilder gsonBuilder) {
        gsonBuilder.registerTypeAdapter(StringList.class, new JSONStringListSerializer());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonElement serialize(StringList src, Type type, JsonSerializationContext context) {
        JsonObject list = new JsonObject();

        JsonArray data = new JsonArray();
        for (String s : src.getStrings()) {
            data.add(s);
        }

        list.add(src.getIdentifier(), data);

        return list;
    }

}
