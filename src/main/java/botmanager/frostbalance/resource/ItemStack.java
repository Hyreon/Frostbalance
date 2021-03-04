package botmanager.frostbalance.resource;

import botmanager.frostbalance.Frostbalance;
import org.jetbrains.annotations.Nullable;

public class ItemStack {

    String resourceId;
    double quantity;
    int quality;
    boolean fictional;

    public ItemStack(ItemType itemType, double quantity) {
        this.resourceId = itemType.getId();
        this.quantity = quantity;
        this.quality = 1; //default natural quality level
    }

    public ItemStack(ItemType itemType, double quantity, int quality, boolean fictional) {
        this.resourceId = itemType.getId();
        this.quantity = quantity;
        this.quality = quality;
        this.fictional = fictional;
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

    /**
     * An unsafe method used to decrease quantity from an item stack.
     * This is NOT safe to use, and will throw an illegal argument exception if the request decrement is too high.
     * @param quantity The amount of the item to remove.
     */
    @Nullable
    public void decrement(double quantity) {
        if (quantity > this.quantity && !fictional) {
            throw new IllegalArgumentException("Tried to remove more items from a stack than the stack had!");
        }
        this.quantity -= quantity;
    }
}
