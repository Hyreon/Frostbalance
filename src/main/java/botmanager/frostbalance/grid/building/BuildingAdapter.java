package botmanager.frostbalance.grid.building;

import com.google.gson.*;

import java.lang.reflect.Type;

public class BuildingAdapter implements JsonSerializer<Building>, JsonDeserializer<Building> {

    @Override
    public JsonElement serialize(Building src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(src.getClass().getSimpleName()));
        result.add("properties", context.serialize(src, src.getClass()));

        return result;
    }

    @Override
    public Building deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonElement element = jsonObject.get("properties");

        //TODO deprecated legacy support
        if (element == null) {
            return context.deserialize(json, Gatherer.class);
        }

        //TODO make this not dependent on the name of the routine / action
        String classPrefix = "botmanager.frostbalance.grid.building.";

        try {
            return context.deserialize(element, Class.forName(classPrefix + type));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown element type: " + type, e);
        }
    }
}
