package botmanager.frostbalance.grid;

import botmanager.frostbalance.Nation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Tile {

    /**
     * The strength of each players' claim to a specific tile. See Claim for details.
     */
    List<Claim> claims = new ArrayList<>();

    /**
     * A list of all objects currently on this tile.
     */
    List<TileObject> objects = new ArrayList<>();

    WorldMap map;
    Hex location;

    private Nation lastOwningNation;
    private PlayerCharacter lastOwningPlayer;

    public Tile(WorldMap map, Hex location) {
        this.map = map;
        this.location = location;
    }

    /**
     * Gets the user who has the strongest relevant claim on this tile.
     * @return The user who owns this tile (or null if there are no active claims).
     */
    public PlayerCharacter getOwningUser() {
        getOwningNation();
        PlayerCharacter selectedPlayer = null;
        Double selectedStrength = 0.0;
        for (Claim claim : claims) {
            if (claim.getNation() != lastOwningNation) continue;
            if (!claim.isActive()) continue;
            if (claim.getStrength() > selectedStrength ||
                    (lastOwningPlayer.equals(claim.getPlayer()) && claim.getStrength() == selectedStrength)) {
                selectedPlayer = claim.getPlayer();
                selectedStrength = claim.getStrength();
            }
        }
        lastOwningPlayer = selectedPlayer;
        return selectedPlayer;
    }

    /**
     * Gets the nation whose members have the strongest combined claim on this tile.
     * Note that this will return NONE every time for world maps outside the global set.
     * @return The nation that owns this tile (or null if there are no national claims).
     */
    public Nation getOwningNation() {
        HashMap<Nation, Double> nationalClaimStrengths = new HashMap<>();
        for (Claim claim : claims) {
            nationalClaimStrengths.put(claim.getNation(), nationalClaimStrengths.getOrDefault(claim.getNation(), 0.0) + claim.getStrength());
        }
        Nation selectedNation = null;
        Double selectedStrength = 0.0;
        for (Nation nation : nationalClaimStrengths.keySet()) {
            if (nationalClaimStrengths.get(nation) > selectedStrength ||
                    (lastOwningNation != null && lastOwningNation.equals(nation) && selectedStrength == nationalClaimStrengths.get(nation))) {
                selectedNation = nation;
                selectedStrength = nationalClaimStrengths.get(nation);
            }
        }
        lastOwningNation = selectedNation;
        return selectedNation;
    }

    /**
     * Adds a claim with the given parameters to this tile.
     * @param player
     * @param nation
     * @param strength
     */
    public Claim addClaim(PlayerCharacter player, Nation nation, Double strength) {
        for (Claim claim : claims) {
            if (claim.overlaps(this, player, nation)) {
                claim.add(strength);
                getOwningNation();
                map.updateStrongestClaim();
                return claim;
            }
        }

        Claim newClaim = new Claim(this, player, nation, strength);
        claims.add(newClaim);
        getOwningNation();
        map.updateStrongestClaim();
        return newClaim;
    }

    public Claim addClaim(PlayerCharacter player, Double strength) {
        return addClaim(player, player.getNation(), strength);
    }

    public Claim addClaim(Claim newClaim) {
        for (Claim claim : claims) {
            if (newClaim.overlaps(claim)) {
                throw new IllegalArgumentException("This claim overlaps an existing claim!");
            }
        }
        claims.add(newClaim);
        getOwningNation();
        map.updateStrongestClaim();
        return newClaim;
    }

    public Claim getClaim(PlayerCharacter player, Nation nation) {
        for (Claim claim : claims) {
            if (claim.overlaps(this, player, nation)) {
                return claim;
            }
        }
        return null;
    }

    /**
     * Reduce the strength of a claim.
     * Any player can do this to their own claims at no cost, but with no refund.
     */
    public double reduceClaim(PlayerCharacter player, Nation nation, Double amount) {
        Claim claim = getClaim(player, nation);
        if (claim == null) {
            return 0.0;
        }
        map.updateStrongestClaim();
        return claim.reduce(amount);
    }

    public Hex getLocation() {
        return location;
    }

    public Double getNationalStrength(Nation nation) {
        Double nationalStrength = 0.0;
        for (Claim claim : claims) {
            if (claim.getNation() == nation) {
                nationalStrength += claim.getStrength();
            }
        }
        System.out.println("National strength: " + nationalStrength);
        return nationalStrength;
    }

    public Double getNationalStrength() {
        HashMap<Nation, Double> nationalClaimStrengths = new HashMap<>();
        for (Claim claim : claims) {
            nationalClaimStrengths.put(claim.getNation(), nationalClaimStrengths.getOrDefault(claim.getNation(), 0.0) + claim.getStrength());
        }
        Double selectedStrength = 0.0;
        for (Nation nation : nationalClaimStrengths.keySet()) {
            if (nationalClaimStrengths.get(nation) > selectedStrength) {
                selectedStrength = nationalClaimStrengths.get(nation);
            }
        }
        return selectedStrength;
    }

    public void moveObject(TileObject tileObject, Hex location) {
        objects.remove(tileObject);
        map.getTile(location).addObject(tileObject);
    }

    public void addObject(TileObject tileObject) {
        objects.add(tileObject);
    }

    public Collection<TileObject> getObjects() {
        return objects;
    }

    public WorldMap getMap() {
        return map;
    }
}
