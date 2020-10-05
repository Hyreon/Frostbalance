package botmanager.frostbalance.resource;

public class ItemStack {

    String resourceId;
    double quantity;
    double quality;

    ItemStack(ItemType itemType, double quantity, double quality) {
        this.resourceId = itemType.getId();
        this.quantity = quantity;
        this.quality = quality;
    }

}
