package botmanager.frostbalance.resource;

import java.awt.*;

public class Resource {

    public String name;
    public Color color;

    public Resource(String name, String colorHexCode) {
        this.name = name;
        this.color = new Color(Integer.parseInt(colorHexCode.replace("0x", "")));
    }

}
