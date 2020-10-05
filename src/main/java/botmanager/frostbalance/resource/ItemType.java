package botmanager.frostbalance.resource;

import java.awt.*;

public class ItemType {

    public String name;
    public Color color;

    public ItemType(String name, String colorHexCode) {
        this.name = name;
        this.color = new Color(Integer.parseInt(colorHexCode.replace("0x", "")));
    }

    public String getId() {
        return name;
    }
}
