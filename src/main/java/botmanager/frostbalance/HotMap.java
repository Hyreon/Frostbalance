package botmanager.frostbalance;

import java.util.HashMap;

/**
 * A HotMap is identical to a HashMap; however, its getOrDefault method
 * will put a value there if there wasn't one before, rather than just returning
 * the default. This should reduce unintended behavior over indexed list maps.
 */
public class HotMap<K, V> extends HashMap<K, V> {

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        V result = super.getOrDefault(key, defaultValue);
        if (super.get(key) == null) {
            super.put((K) key, result);
        }
        return result;
    }
}
