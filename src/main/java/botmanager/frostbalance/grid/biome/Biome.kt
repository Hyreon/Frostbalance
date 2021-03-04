package botmanager.frostbalance.grid.biome

import java.awt.Color

/**
 * Represents a specific biome. Each one has a unique appearance and movement properties.
 */
class Biome(val name: String, val color: Color,
            val elevation: ElevationClass, val temperature: TemperatureClass, val humidity: HumidityClass,
            val movementCost: Int = 1, val environment: Environment = Environment.LAND) {

    override fun toString(): String {
        return name
    }

    companion object {

        //unknown
        val UNKNOWN = Biome("UNKNOWN", Color(0, 0, 0), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE, 1, Environment.VOID)

        //overlay
        @JvmField
        val RIVER = Biome("RIVER", Color(80, 160, 255), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE, 1, Environment.SEA)
        @JvmField
        val COAST = Biome("COAST", Color(80, 120, 255), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE, 1, Environment.SEA)

        //val ISLAND = Biome("UNKNOWN", Color(80, 255, 80), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE)
        //val BEACH = Biome("UNKNOWN", Color(255, 255, 80), ElevationClass.NONE, TemperatureClass.NONE, HumidityClass.NONE)


        var biomes: Array<Biome> = arrayOf(UNKNOWN, RIVER, COAST)

        private var smartMap: MutableMap<ElevationClass, MutableMap<TemperatureClass, MutableMap<HumidityClass, Biome>>> = mutableMapOf()

        fun updateSmartMap() {
            smartMap = makeSmartMap(biomes)
        }

        private fun makeSmartMap(array: Array<Biome>): MutableMap<ElevationClass, MutableMap<TemperatureClass, MutableMap<HumidityClass, Biome>>> {
            return ElevationClass.values().associateWith { elevation ->
                TemperatureClass.values().associateWith { temperature ->
                    HumidityClass.values().associateWith { humidity ->
                        bestFit(array, elevation, temperature, humidity)
                    }.toMutableMap()
                }.toMutableMap()
            }.toMutableMap()
        }

        private fun bestFit(array: Array<Biome>, initElev: ElevationClass, initTemp: TemperatureClass, initWatr: HumidityClass): Biome {

            val elevations = arrayOf(initElev).plus(ElevationClass.values().copyOfRange(initElev.ordinal + 1, ElevationClass.values().size))
            val temperatures = arrayOf(initTemp).plus(TemperatureClass.values().copyOfRange(initTemp.ordinal + 1, TemperatureClass.values().size))
            val humidities = arrayOf(initWatr).plus(HumidityClass.values().copyOfRange(initWatr.ordinal + 1, HumidityClass.values().size))

            if (initElev == ElevationClass.NONE || initTemp == TemperatureClass.NONE || initWatr == HumidityClass.NONE) return UNKNOWN

            for (elevation in elevations) {
                for (temperature in temperatures) {
                    for (humidity in humidities) {
                        array.firstOrNull { biome -> biome.elevation == elevation && biome.temperature == temperature && biome.humidity == humidity }?.run {
                            return this
                        }
                    }
                }
            }

            return UNKNOWN
        }

        @JvmStatic
        fun from(elevation: ElevationClass, temperature: TemperatureClass, humidity: HumidityClass): Biome {
            return smartMap.get(elevation)?.get(temperature)?.get(humidity) ?: run {
                System.err.println("Smart map was found to be missing a biome!")
                UNKNOWN
            }
        }

        fun fromName(key: String): Biome {
            for (biome in biomes) {
                if (biome.name == key) {
                    return biome
                }
            }
            return UNKNOWN
        }
    }

    enum class Environment {
        LAND, SEA, VOID
    }

}