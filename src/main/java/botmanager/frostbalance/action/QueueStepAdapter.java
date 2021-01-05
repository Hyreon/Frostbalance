package botmanager.frostbalance.action;

import com.google.gson.*;

import java.lang.reflect.Type;

public class QueueStepAdapter implements JsonSerializer<QueueStep>, JsonDeserializer<QueueStep> {

    @Override
    public JsonElement serialize(QueueStep src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(src.getClass().getSimpleName()));
        result.add("properties", context.serialize(src, src.getClass()));

        return result;
    }

    @Override
    public QueueStep deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonElement element = jsonObject.get("properties");

        //TODO make this not dependent on the name of the routine / action
        String classPrefix;
        if (type.contains("Action")) {
            classPrefix = "botmanager.frostbalance.action.actions.";
        } else {
            classPrefix = "botmanager.frostbalance.action.routine.";
        }

        try {
            return context.deserialize(element, Class.forName(classPrefix + type));
        } catch (ClassNotFoundException e) {
            throw new JsonParseException("Unknown element type: " + type, e);
        }
    }
}
