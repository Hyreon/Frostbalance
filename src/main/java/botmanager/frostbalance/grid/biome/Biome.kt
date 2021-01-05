package botmanager.frostbalance.grid.biome

import java.awt.Color

enum class Biome(var color: Color, val elevation: ElevationClass, val temperature: TemperatureClass, val humidity: HumidityClass) {

    //unknown
    UNKNOWN(Color(0, 0, 0), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE),

    //overlay
    RIVER(Color(80, 160, 255), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE),
    ISLAND(Color(80, 255, 80), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE),
    BEACH(Color(255, 255, 80), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE),
    COAST(Color(80, 120, 255), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE),

    //sea
    SEA(Color(80, 80, 255), ElevationClass.SEA, TemperatureClass.COOL, HumidityClass.ARID),
    ICE(Color(192, 255, 255), ElevationClass.SEA, TemperatureClass.BOREAL, HumidityClass.ARID),
    STORMY_SEA(Color(60, 0, 200), ElevationClass.SEA, TemperatureClass.TROPICAL, HumidityClass.HEAVY_RAIN),

    //mountain
    SNOW_PEAK(Color(220, 200, 240), ElevationClass.MOUNTAINS, TemperatureClass.BOREAL, HumidityClass.ARID), //covers all humidities
    ROCKY(Color(180, 160, 160), ElevationClass.MOUNTAINS, TemperatureClass.COOL, HumidityClass.ARID),
    ALP(Color(150, 180, 150), ElevationClass.MOUNTAINS, TemperatureClass.COOL, HumidityClass.GOOD_RAIN),
    MESA(Color(192, 128, 64), ElevationClass.MOUNTAINS, TemperatureClass.TROPICAL, HumidityClass.ARID), //covers all humidities

    //cold
    TUNDRA(Color(0xFAFADC), ElevationClass.PLAINS, TemperatureClass.BOREAL, HumidityClass.ARID),
    SNOW_PLAINS(Color(0xCCD9A4), ElevationClass.PLAINS, TemperatureClass.BOREAL, HumidityClass.MODEST_RAIN),
    TAIGA(Color(0x008080), ElevationClass.PLAINS, TemperatureClass.BOREAL, HumidityClass.GOOD_RAIN),
    MARSH(Color(0x606080), ElevationClass.PLAINS, TemperatureClass.BOREAL, HumidityClass.HEAVY_RAIN),

    //moderate
    TEMPERATE_DESERT(Color(0x89A32E), ElevationClass.PLAINS, TemperatureClass.COOL, HumidityClass.ARID),
    STEPPE(Color(0x69932E), ElevationClass.PLAINS, TemperatureClass.COOL, HumidityClass.MODEST_RAIN),
    FOREST(Color(0x008000), ElevationClass.PLAINS, TemperatureClass.COOL, HumidityClass.GOOD_RAIN),
    SWAMP(Color(0x606000), ElevationClass.PLAINS, TemperatureClass.COOL, HumidityClass.HEAVY_RAIN),

    //hot
    DESERT(Color(0xF0C896), ElevationClass.PLAINS, TemperatureClass.TROPICAL, HumidityClass.ARID),
    PRAIRIE(Color(0xA0BE64), ElevationClass.PLAINS, TemperatureClass.TROPICAL, HumidityClass.MODEST_RAIN),
    SAVANNA(Color(0x808000), ElevationClass.PLAINS, TemperatureClass.TROPICAL, HumidityClass.GOOD_RAIN),
    JUNGLE(Color(0x30A050), ElevationClass.PLAINS, TemperatureClass.TROPICAL, HumidityClass.HEAVY_RAIN);

    companion object {
        val SMART_MAP: Map<ElevationClass, Map<TemperatureClass, Map<HumidityClass, Biome>>> = makeSmartMap(values()).let { println(it); it }

        private fun makeSmartMap(array: Array<Biome>): Map<ElevationClass, Map<TemperatureClass, Map<HumidityClass, Biome>>> {
            return ElevationClass.values().associateWith { elevation ->
                TemperatureClass.values().associateWith { temperature ->
                    HumidityClass.values().associateWith { humidity ->
                        bestFit(array, elevation, temperature, humidity)
                    }
                }
            }
        }

        private fun bestFit(array: Array<Biome>, initElev: ElevationClass, initTemp: TemperatureClass, initWatr: HumidityClass): Biome {

            val elevations = arrayOf(initElev).plus(ElevationClass.values().copyOfRange(initElev.ordinal + 1, ElevationClass.values().size))
            val temperatures = arrayOf(initTemp).plus(TemperatureClass.values().copyOfRange(initTemp.ordinal + 1, TemperatureClass.values().size))
            val humidities = arrayOf(initWatr).plus(HumidityClass.values().copyOfRange(initWatr.ordinal + 1, HumidityClass.values().size))

            if (initElev == ElevationClass.NONE || initTemp == TemperatureClass.NONE || initWatr == HumidityClass.NONE) return UNKNOWN

            for (elevation in elevations) {
                for (temperature in temperatures) {
                    for (humidity in humidities) {
                        if (initElev == ElevationClass.HILLS) {
                            println("Testing $elevation $temperature $humidity")
                        }
                        array.firstOrNull { biome -> biome.elevation == elevation && biome.temperature == temperature && biome.humidity == humidity }?.run {
                            return this
                        }
                    }
                }
            }

            return UNKNOWN
        }
    }

}