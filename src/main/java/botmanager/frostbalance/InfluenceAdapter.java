package botmanager.frostbalance;

import com.google.gson.*;

import java.lang.reflect.Type;

public class InfluenceAdapter implements JsonDeserializer<Influence>, JsonSerializer<Influence> {

    @Override
    public Influence deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new Influence(json.getAsDouble());
    }

    @Override
    public JsonElement serialize(Influence src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.getValue());
    }
}
