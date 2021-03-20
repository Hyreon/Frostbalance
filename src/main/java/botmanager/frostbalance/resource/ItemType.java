package botmanager.frostbalance.resource;

import botmanager.frostbalance.resource.crafting.ItemModifier;

import java.awt.*;
import java.util.Collection;

public class ItemType {

    public static ItemType DEBUG = new ItemType("MISSING_ITEM", "0xFF00FF");

    public String name;
    public Color color;

    public ItemType(String name, String colorHexCode) {
        this.name = name;
        this.color = new Color(Integer.parseInt(colorHexCode.replace("0x", ""), 16));
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return name;
    }

    public String toString() {
        return name;
    }

    public boolean isDescribedBy(Collection<ItemModifier> collection) {
        return true;
    }
}
