package botmanager.frostbalance

import java.util.*
import kotlin.jvm.Throws

/**
 * A HotMap is identical to a HashMap; however, its getOrDefault method
 * will put a value there if there wasn't one before, rather than just returning
 * the default. This should reduce unintended behavior over indexed list maps.
 */
class HotMap<K, V> : HashMap<K, V>() {
    @Throws(ClassCastException::class)
    override fun getOrDefault(key: K, defaultValue: V): V {
        val result = super.getOrDefault(key, defaultValue)
        if (super.get(key) == null) {
            super.put(key as K, result)
        }
        return result
    }
}