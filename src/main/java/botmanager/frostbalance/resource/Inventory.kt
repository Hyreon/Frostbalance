package botmanager.frostbalance.resource

class Inventory {
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

    fun render(): String? {
        val builder = StringBuilder()
        for (item in items) {
            builder.append(item, "\n")
        }
        return items.toString()
    }
}