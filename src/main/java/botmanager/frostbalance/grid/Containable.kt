package botmanager.frostbalance.grid
//TODO use this!
/**
 * An object that contains transient references to another object.
 * @param <T> The type of object that can contain it
</T> */
interface Containable<T> {
    fun setParent(parent: T)
}