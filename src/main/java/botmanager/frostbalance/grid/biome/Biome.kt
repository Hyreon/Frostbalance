package botmanager.frostbalance.grid.biome

import java.awt.Color

enum class Biome(var color: Color) {

    //sea
    COAST(Color(80, 120, 255)),
    SEA(Color(80, 80, 255)),
    ICE(Color(192, 255, 255)),
    STORMY_SEA(Color(60, 0, 200)),

    //special
    RIVER(Color(80, 160, 255)),
    ISLAND(Color(80, 255, 80)),
    BEACH(Color(255, 255, 80)),

    //mountain
    SNOW_PEAK(Color(220, 200, 240)),
    ROCKY(Color(180, 160, 160)),
    ALP(Color(150, 180, 150)),
    MESA(Color(192, 128, 64)),

    //cold
    TUNDRA(Color(0xFAFADC)),
    SNOW_FIELD(Color(0xCCD9A4)),
    TAIGA(Color(0x008080)),
    MARSH(Color(0x606080)),

    //moderate
    TEMPERATE_DESERT(Color(0x89A32E)),
    GRASSLANDS(Color(0x69932E)),
    FOREST(Color(0x008000)),
    SWAMP(Color(0x606000)),

    //hot
    DESERT(Color(0xF0C896)),
    PRAIRIE(Color(0xA0BE64)),
    SAVANNA(Color(0x808000)),
    JUNGLE(Color(0x30A050));

}