package botmanager.frostbalance.resource

class Inventory(val fictional: Boolean = false) {
    var items: MutableList<ItemStack> = mutableListOf()

    /**
     * Adds an item to the inventory. Has no effect if adding null.
     */
    fun addItem(item: ItemStack?) {
        item?.let {
            items.firstOrNull { it.resourceId == item.resourceId && it.quality == item.quality }
                ?.increment(item.quantity)
                ?: items.add(item)
        }
    }

    /**
     * Removes an item from the inventory. Has no effect if removing null.
     * This is generally used for trades and other automated processes. If actual item stacks
     * are ever introduced, this is NOT the method that will handle their removal.
     */
    fun removeItem(item: ItemStack?) {
        println("Removing item $item")
        item?.let { item ->
            if (fictional || queryItem(item)) {
                while (item.quantity > 0) {
                    println("Running through loop")
                    var substitutionDegree = 0
                    var itemToRemove: ItemStack?
                    do {
                        if (!items.any{it.resourceId == item.resourceId && it.quality - substitutionDegree >= item.quality}) {
                            if (fictional) {
                                println("No more matching items found")
                                return
                            }
                            throw IllegalStateException("Item validation failed - unable to find the items to get rid of during an inventory removal!")
                        }
                        itemToRemove = items.firstOrNull { itemToRemove -> itemToRemove.resourceId == item.resourceId && itemToRemove.quality - substitutionDegree == item.quality }
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
    fun queryItem(item: ItemStack, substitutions: Boolean = false): Boolean {
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