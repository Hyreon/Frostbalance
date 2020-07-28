package botmanager.frostbalance.grid;

import com.google.gson.JsonDeserializer;

/**
 * An object that contains objects with transient references to this object.
 * @param <T> The type of object it contains.
 */
public interface Container<T> extends JsonDeserializer<T> {
}
