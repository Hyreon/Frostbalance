package botmanager.frostbalance.grid;

import botmanager.frostbalance.Nation;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ClaimData extends TileData implements Container<Claim> {

    /**
     * The strength of each players' claim to a specific tile. See Claim for details.
     */
    List<Claim> claims = new ArrayList<>();

    private Nation lastOwningNation = Nation.NONE;
    private String lastOwningUserId;

    public ClaimData(Tile tile) {
        super(tile);
    }

    public Tile getTile() {
        return tile;
    }

    /**
     * Gets the user who has the strongest relevant claim on this tile.
     * @return The user who owns this tile (or null if there are no active claims).
     */
    public String getOwningUserId() {
        if (getClaims().isEmpty()) return null;
        getOwningNation();
        String selectedUserId = null;
        Double selectedStrength = 0.0;
        for (Claim claim : getActiveClaims()) {
            if (claim.getNation() != lastOwningNation) continue;
            if (!claim.isActive()) continue;
            if (claim.getStrength() > selectedStrength ||
                    (lastOwningUserId.equals(claim.getUserId()) && claim.getStrength() == selectedStrength)) {
                selectedUserId = claim.getUserId();
                selectedStrength = claim.getStrength();
            }
        }
        lastOwningUserId = selectedUserId;
        return selectedUserId;
    }

    /**
     * Gets the nation whose members have the strongest combined claim on this tile.
     * Note that this will return NONE every time for world maps outside the global set.
     * @return The nation that owns this tile (or NONE if there are no national claims).
     */
    public Nation getOwningNation() {
        if (getClaims().isEmpty()) return Nation.NONE;
        Nation selectedNation = lastOwningNation;
        Double selectedStrength = getNationalStrength(lastOwningNation);

        for (Nation nation : Nation.getNations()) {
            Double nationalStrength = getNationalStrength(nation);
            if (nationalStrength > selectedStrength) {
                selectedNation = nation;
                selectedStrength = nationalStrength;
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
        for (Claim claim : getClaims()) {
            if (claim.overlaps(this, player, nation)) {
                claim.add(strength);
                getOwningNation();
                getTile().getMap().updateStrongestClaim();
                return claim;
            }
        }

        Claim newClaim = new Claim(player, nation, strength);
        getOwningNation();
        getTile().getMap().updateStrongestClaim();
        return newClaim;
    }

    public Claim addClaim(PlayerCharacter player, Double strength) {
        return addClaim(player, player.getNation(), strength);
    }

    public Claim addClaim(Claim newClaim) {
        for (Claim claim : getClaims()) {
            if (newClaim.overlaps(claim)) {
                throw new IllegalArgumentException("This claim overlaps an existing claim!");
            }
        }
        claims.add(newClaim);
        getOwningNation();
        getTile().getMap().updateStrongestClaim();
        return newClaim;
    }

    public Claim getClaim(PlayerCharacter player, Nation nation) {
        for (Claim claim : getClaims()) {
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
        getTile().getMap().updateStrongestClaim();
        return claim.reduce(amount);
    }

    public Double getNationalStrength(Nation nation) {
        if (nation == null) return 0.0;
        Double nationalStrength = 0.0;
        for (Claim claim : getActiveClaims()) {
            if (claim.getNation() == nation) {
                nationalStrength += claim.getStrength();
            }
        }
        return nationalStrength;
    }

    public Double getNationalStrength() {
        HashMap<Nation, Double> nationalClaimStrengths = new HashMap<>();
        for (Claim claim : getActiveClaims()) {
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

    public Collection<Claim> getClaims() {
        return claims;
    }

    public Collection<Claim> getActiveClaims() {
        List<Claim> activeClaims = new ArrayList<>();
        for (Claim claim : claims) {
            if (claim.isActive()) activeClaims.add(claim);
        }
        return activeClaims;
    }

    @Override
    public Claim deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Claim claim = context.deserialize(json, ClaimData.class);
        claim.claimData = this;
        return claim;
    }

    public String getClaimList(boolean tutorial) {

        List<String> lines = new ArrayList<>();
        Nation owningNation = getOwningNation();
        if (owningNation != null) {
            for (Nation nation : Nation.getNations()) {
                Double strength = getNationalStrength(nation);
                if (strength == 0.0) continue;
                String effectiveString;
                if (tutorial) {
                    effectiveString = nation.toString() + ": " + String.format("%.3f", strength);
                } else {
                    effectiveString = nation.getEffectiveName() + ": " + String.format("%.3f", strength);
                }
                if (owningNation == nation) {
                    lines.add("**" + effectiveString + "**");
                } else {
                    lines.add(effectiveString);
                }
            }
        }
        return String.join("\n", lines);

    }
}
