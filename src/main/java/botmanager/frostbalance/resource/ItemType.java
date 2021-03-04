package botmanager.frostbalance.resource;

import java.awt.*;

public class ItemType {

    public static ItemType DEBUG = new ItemType("MISSING_ITEM", "0xFF00FF");

    public String name;
    public Color color;

    public ItemType(String name, String colorHexCode) {
        this.name = name;
        this.color = new Color(Integer.parseInt(colorHexCode.replace("0x", ""), 16));
    }

    public String getId() {
        return name;
    }

    public String toString() {
        return name;
    }
}
