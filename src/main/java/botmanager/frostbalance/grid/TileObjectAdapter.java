package botmanager.frostbalance.grid;

import com.google.gson.*;

import java.lang.reflect.Type;

public class TileObjectAdapter implements JsonSerializer<TileObject>, JsonDeserializer<TileObject> {

    @Override
    public JsonElement serialize(TileObject src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(src.getClass().getSimpleName()));
        result.add("properties", context.serialize(src, src.getClass()));

        return result;
    }

    @Override
    public TileObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        try {
            String type = jsonObject.get("type").getAsString();
            JsonElement element = jsonObject.get("properties");

            try {
                TileObject tileObject = context.deserialize(element, Class.forName("botmanager.frostbalance.grid." + type));
                if (tileObject instanceof PlayerCharacter) {
                    PlayerCharacter.cache.add((PlayerCharacter) tileObject);
                }
                return tileObject;
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Unknown element type: " + type, e);
            }
        } catch (NullPointerException e) {
            System.err.println("Nullpointer when loading json element: " + json);
            return null;
        }

    }
}
