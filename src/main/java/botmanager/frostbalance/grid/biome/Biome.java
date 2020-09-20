package botmanager.frostbalance.grid.biome;

import java.awt.*;

public enum Biome {

    SEA(new Color(80, 80, 255)),
    ICE(new Color(192, 255, 255)),
    STORMY_SEA(new Color(60, 0, 200)),
    SNOW_MOUNTAIN(new Color(220, 200, 240)),
    MOUNTAIN(new Color(180, 160, 160)),
    MESA(new Color(192, 128, 64)),
    SNOW_FIELD(new Color(0xCCD9A4)),
    GRASSLANDS(new Color(0x69932E)),
    PRAIRIE(new Color(0xA0BE64)),
    ARID_DESERT(new Color(0xF0C896)),
    DESERT(new Color(0xD2C78A)),
    DRY_GRASSLANDS(new Color(0x89A32E)),
    TUNDRA(new Color(0xFAFADC)),
    RAINFOREST(new Color(0x30A050)),
    JUNGLE(new Color(0x10C010)),
    SAVANNA(new Color(0x808000)),
    FOREST(new Color(0x008000)),
    TAIGA(new Color(0x008080)),
    SWAMP(new Color(0x606000)),
    MARSH(new Color(0x606080));
    Color color;

    Biome(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
