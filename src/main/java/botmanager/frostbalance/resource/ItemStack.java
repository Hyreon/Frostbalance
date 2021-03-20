package botmanager.frostbalance.resource;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.resource.crafting.ItemModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ItemStack {

    String resourceId;
    double quantity;
    int quality;
    boolean fictional;

    List<ItemModifier> modifiers;

    //TODO run special modifier code, if present
    public ItemStack(ItemType itemType, double quantity, List<ItemModifier> modifiers) {
        this(itemType, quantity, 1, false, modifiers);
    }

    public ItemStack(ItemType itemType, double quantity) {
        this(itemType, quantity, 1, false);
    }

    public ItemStack(ItemType itemType, double quantity, int quality, boolean fictional) {
        this(itemType, quantity, quality, fictional, new ArrayList<>());
    }

    public ItemStack(ItemType itemType, double quantity, int quality, boolean fictional, List<ItemModifier> modifiers) {
        if (!itemType.isDescribedBy(modifiers)) {
            throw new IllegalStateException("Tried to create an ItemStack that can't exist!");
        }
        this.resourceId = itemType.getId();
        this.quantity = quantity;
        this.quality = quality;
        this.fictional = fictional;
        this.modifiers = modifiers;
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
