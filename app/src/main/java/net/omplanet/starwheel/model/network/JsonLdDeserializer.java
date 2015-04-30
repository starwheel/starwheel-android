package net.omplanet.starwheel.model.network;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.omplanet.starwheel.model.domain.Thing;

import java.lang.reflect.Type;

public class JsonLdDeserializer implements JsonDeserializer<Thing> {

    public Thing deserialize(JsonElement json, Type typeOfT,
                                       JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        Class<?> clazz = null;
        try {
            String type = "net.omplanet.starwheel.model.domain." + jsonObject.get("@type").getAsString();
            clazz = Class.forName(type);
        } catch (Exception e) {
            e.printStackTrace();
            throw new JsonParseException("Unknown class to deserialize: " + e.getMessage());
        }

        return context.deserialize(json, clazz);
    }

}