package botmanager.frostbalance.resource;

public class HousingInventory extends Inventory {

    //TODO check if an item is furniture; non-furniture items cannot be stored in housing
    @Override
    public boolean canAddItemStack(ItemStack itemStack) {
        return false;
    }

}
