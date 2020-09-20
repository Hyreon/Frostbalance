package botmanager.frostbalance.grid.biome;

import java.awt.*;

public enum Biome {

    SEA(new Color(80, 80, 255)),
    ICE(Color.CYAN),
    STORMY_SEA(new Color(60, 0, 200)),
    SNOW_MOUNTAIN(new Color(220, 200, 240)),
    MOUNTAIN(new Color(180, 160, 160)),
    MESA(new Color(192, 128, 64)),
    GRASSLANDS(new Color(0x69932E)),
    SHRUBLANDS(new Color(0xA0BE64)),
    ARID_DESERT(new Color(0xF0C896)),
    DESERT(new Color(0xD2C78A)),
    TUNDRA(new Color(0xFAFADC)),
    JUNGLE(new Color(0x40A040)),
    SAVANNA(new Color(0x408000)),
    FOREST(new Color(0x008000)),
    TAIGA(new Color(0x008040)),
    SWAMP(new Color(0x606030));
    Color color;

    Biome(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
