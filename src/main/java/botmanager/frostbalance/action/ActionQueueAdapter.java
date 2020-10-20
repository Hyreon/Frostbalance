package botmanager.frostbalance.action;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ActionQueueAdapter implements JsonDeserializer<ActionQueue> {

    @Override
    public ActionQueue deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        ActionQueue queue = new ActionQueue();
        for (JsonElement element : jsonElement.getAsJsonArray()) {
            queue.add(context.deserialize(element, QueueStep.class));
        }
        return queue;
    }

}
