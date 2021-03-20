package botmanager.frostbalance.resource.crafting;

import botmanager.frostbalance.resource.Inventory;

public abstract class CraftingModifier {

    String name;

    /**
     * Special effects to perform when applying this modifier to a different item.
     * @param inventory the inventory this is crafted in
     */
    public abstract void onCraft(Inventory inventory);

}
