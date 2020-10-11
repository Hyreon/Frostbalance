package botmanager.frostbalance.resource

class Inventory {
    var items: MutableList<ItemStack> = mutableListOf()

    fun addItem(item: ItemStack) {
        items.firstOrNull { it.resourceId == item.resourceId && it.quality == item.quality }
                ?.increment(item.quantity)
                ?: items.add(item)
    }

    fun render(): String? {
        val builder = StringBuilder()
        for (item in items) {
            builder.append(item, "\n")
        }
        return items.toString()
    }
}