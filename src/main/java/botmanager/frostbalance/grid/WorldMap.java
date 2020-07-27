package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.OptionFlag;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

/**
 * A worldmap consists of a set of tiles and all data relevant to them.
 */
public class WorldMap {

    static List<WorldMap> worldMaps = new ArrayList<>();

    Double strongestNationalClaim = Double.MIN_NORMAL;

    public static WorldMap get(Guild guild) {
        if (Frostbalance.bot.getDebugFlags(guild).contains(OptionFlag.MAIN)) {
            guild = null;
        }
        for (WorldMap map : worldMaps) {
            if (map.getGuild() == null) {
                if (guild == null) return map;
            } else if (map.getGuild().equals(guild)) {
                return map;
            }
        }
        WorldMap newMap = new WorldMap(guild);
        worldMaps.add(newMap);
        return newMap;
    }

    List<Tile> loadedTiles = new ArrayList<>();

    /**
     * The guild this map is tied to.
     * If null, that means this is the main/global map.
     */
    Guild guild;

    public WorldMap(Guild guild) {
        this.guild = guild;
    }

    /**
     * The same as get tile, but the default tile it returns will
     * *not* be cached in the grid. Do NOT use this for functions that
     * change the tile!
     * @param hex
     * @return
     */
    @Deprecated
    public Tile getTileLazy(Hex hex) {
        for (Tile tile : loadedTiles) {
            if (tile.getLocation().equals(hex)) {
                return tile;
            }
        }
        return new Tile(this, hex);
    }

    public Tile getTile(Hex hex) {
        for (Tile tile : loadedTiles) {
            if (tile.getLocation().equals(hex)) {
                return tile;
            }
        }
        Tile newTile = new Tile(this, hex);
        loadedTiles.add(newTile);
        return newTile;
    }

    public double getStrongestClaim() {
        System.out.println("Strongest claim: " + strongestNationalClaim);
        return strongestNationalClaim;
    }

    protected void updateStrongestClaim() {
        strongestNationalClaim = Double.MIN_NORMAL;
        for (Tile tile : loadedTiles) {
            Double nationalStrength = tile.getNationalStrength();
            if (nationalStrength > strongestNationalClaim) {
                strongestNationalClaim = nationalStrength;
            }
        }
    }

    /**
     * Tests whether this map is part of the main map, shared between
     * the three main servers.
     * @return Whether this is the main map
     */
    public boolean isMainMap() {
        if (guild == null) {
            return true;
        } else return false;
    }

    public Guild getGuild() {
        return guild;
    }
}
