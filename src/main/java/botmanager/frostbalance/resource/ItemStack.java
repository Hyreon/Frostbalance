package botmanager.frostbalance.resource;

import botmanager.frostbalance.Frostbalance;
import org.jetbrains.annotations.Nullable;

public class ItemStack {

    String resourceId;
    double quantity;
    int quality;

    ItemStack(ItemType itemType, double quantity, int quality) {
        this.resourceId = itemType.getId();
        this.quantity = quantity;
        this.quality = quality;
    }

    @Override
    public String toString() {
        return getResource().name + " x" + quantity + " l" + quality;
    }

    public ItemType getResource() {
        return Frostbalance.bot.itemWithId(resourceId);
    }

    @Nullable
    public void increment(double quantity) {
        this.quantity += quantity;
    }
}
