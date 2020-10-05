package botmanager.frostbalance.resource;

import java.util.List;

public class Inventory {

    List<ItemStack> items;



    public void addItem(ItemStack item) {
        items.add(item);
    }
}
