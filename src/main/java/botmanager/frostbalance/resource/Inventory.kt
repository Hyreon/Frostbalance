package botmanager.frostbalance.resource

/**
 * A list of items with special handling for adding new stacks.
 * Fictional inventories have less safeguards - they allow removing items without
 * actually having the proper supply, for example. As such these are not recommended
 * for normal use.
 */
open class Inventory(val fictional: Boolean = false) {

    var items: MutableList<ItemStack> = mutableListOf()

    constructor(fictional: Boolean, items: MutableList<ItemStack>) : this(fictional) {
        this.items = items
    }

    /**
     * Adds an item to the inventory. Has no effect if adding null.
     */
    fun addItem(item: ItemStack?): Boolean {
        return if (!canAddItemStack(item)) {
            false
        } else {
            item?.let {
                items.firstOrNull { it.resourceId == item.resourceId && it.quality == item.quality }
                    ?.increment(item.quantity)
                    ?: items.add(item)
            }
            true
        }
    }

    open fun canAddItemStack(item: ItemStack?): Boolean {
        return true
    }

    open fun makeFiction(): Inventory {
        return Inventory(true, items)
    }

    open fun loadFiction(fiction: Inventory) {
        items = fiction.items
    }

    /**
     * Removes an item from the inventory. Has no effect if removing null.
     * This is generally used for trades and other automated processes. If actual item stacks
     * are ever introduced, this is NOT the method that will handle their removal.
     */
    fun removeItem(nullableItem: ItemStack?) {
        println("Removing item $nullableItem")
        nullableItem?.let { item ->
            if (fictional || hasItem(item)) {
                while (item.quantity > 0) {
                    var substitutionDegree = 0
                    var itemToRemove: ItemStack?
                    do {
                        if (!items.any{it.resourceId == item.resourceId && it.quality - substitutionDegree >= item.quality}) {
                            if (fictional) {
                                return
                            }
                            throw IllegalStateException("Item validation failed - unable to find the items to get rid of during an inventory removal!")
                        }
                        itemToRemove = items.firstOrNull { candidateItem -> candidateItem.resourceId == item.resourceId && candidateItem.quality - substitutionDegree == item.quality }
                        substitutionDegree++
                    } while (itemToRemove == null)
                    if (itemToRemove.quantity <= item.quantity) {
                        item.decrement(itemToRemove.quantity)
                        items.remove(itemToRemove)
                    } else {
                        itemToRemove.decrement(item.quantity)
                        break
                    }
                }
            } else {
                throw IllegalStateException("Tried to remove items from an inventory that weren't there!")
            }
        }

    }

    @JvmOverloads
    fun hasItem(item: ItemStack, substitutions: Boolean = false): Boolean {
        return items.filter { it.resourceId == item.resourceId &&
                (it.quality == item.quality || (substitutions && it.quality >= item.quality)) }
            .map { it.quantity }
            .reduceOrNull { acc, it -> acc + it } ?: 0.0 > item.quantity
    }

    fun render(): String? {
        val builder = StringBuilder()
        for (item in items) {
            builder.append(item, "\n")
        }
        return items.toString()
    }
}