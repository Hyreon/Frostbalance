package botmanager

import java.time.LocalDateTime

/**
 * A function wrapper that stores data for later use.
 * This is similar, but not exactly the same, as the "by lazy"
 * delegated property provided by Kotlin, because it can still be updated
 * later so long as you force it to update.
 */
class Cache<T>(private val getter: () -> T) {

    private var lastUpdateTime: LocalDateTime = LocalDateTime.now()
    private var lastValue: T? = null

    private fun update() {
        lastUpdateTime = LocalDateTime.now()
        lastValue = getter()
    }

    fun retrieve(timestamp: LocalDateTime): T {
        if (lastUpdateTime < timestamp) {
            update()
        } else if (lastValue == null) {
            update()
        }
        return lastValue as T
    }

    fun retrieve(): T {
        if (lastValue == null) {
            lastValue = getter()
        }
        return lastValue as T
    }

}
