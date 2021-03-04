package botmanager.frostbalance.grid.biome;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A group of similar biomes that have similar appearance and properties.
 */
public class BiomeGroup {

    public static Map<String, BiomeGroup> biomeGroups = technicalBiomeGroups();

    private static Map<String, BiomeGroup> technicalBiomeGroups() {
        HashMap<String, BiomeGroup> tbg = new HashMap<>();
        BiomeGroup riverGroup = new BiomeGroup(Biome.RIVER.getName());
        riverGroup.add(Biome.RIVER);
        tbg.put(Biome.RIVER.getName(), riverGroup);
        BiomeGroup coastGroup = new BiomeGroup(Biome.COAST.getName());
        coastGroup.add(Biome.COAST);
        tbg.put(Biome.COAST.getName(), coastGroup);
        return tbg;
    }

    String groupId;
    Set<Biome> includedBiomes = new HashSet<>();

    public BiomeGroup(String groupId) {
        this.groupId = groupId;
    }

    public void add(Biome biome) {
        includedBiomes.add(biome);
    }

    public static BiomeGroup fromId(String id) {
        return biomeGroups.get(id);
    }

    /**
     *
     * @param group
     * @return true, if this is the first map. false if a previous entry was removed,
     * which may cause issues later down the pipe.
     */
    public static boolean addGroup(BiomeGroup group) {
        return biomeGroups.put(group.groupId, group) == null;
    }

    public Set<Biome> getBiomes() {
        return includedBiomes;
    }

    public String toString() {
        return groupId + ":" + biomeGroups;
    }
}
