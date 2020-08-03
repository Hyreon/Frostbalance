package botmanager.frostbalance.grid;

import botmanager.Utils;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.Nation;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

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
        Influence selectedStrength = new Influence(0);
        for (Claim claim : getActiveClaims()) {
            if (claim.getNation() != lastOwningNation) continue;
            if (!claim.isActive()) continue;
            if (claim.getStrength().compareTo(selectedStrength) > 0 ||
                    (lastOwningUserId.equals(claim.getUserId()) && claim.getStrength().equals(selectedStrength))) {
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
        Influence selectedStrength = getNationalStrength(lastOwningNation);

        for (Nation nation : Nation.getNations()) {
            Influence nationalStrength = getNationalStrength(nation);
            if (nationalStrength.compareTo(selectedStrength) > 0) {
                selectedNation = nation;
                selectedStrength = nationalStrength;
            }
        }
        if (selectedStrength.getThousandths() <= 0) {
            return Nation.NONE;
        }
        lastOwningNation = selectedNation;
        return selectedNation;
    }

    public String getOwningNationName() {
        if (getTile().getMap().isTutorialMap()) {
            return getOwningNation().toString();
        } else if (getTile().getMap().isMainMap()) {
            return getOwningNation().getEffectiveName();
        } else {
            return "Wildnerness";
        }
    }

    /**
     * Adds a claim with the given parameters to this tile.
     * @param player
     * @param nation
     * @param strength
     */
    public Claim addClaim(PlayerCharacter player, Nation nation, Influence strength) {
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

    public Claim addClaim(PlayerCharacter player, Influence strength) {
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
     * @return
     */
    public Influence reduceClaim(PlayerCharacter player, Nation nation, Influence amount) {
        Claim claim = getClaim(player, nation);
        if (claim == null) {
            return new Influence(0);
        }
        getTile().getMap().updateStrongestClaim();
        return claim.reduce(amount);
    }

    //TODO this assumes a single claim per player.
    private Influence getUserStrength(String userId) {
        for (Claim claim : getActiveClaims()) {
            if (claim.getUserId().equals(userId)) {
                return claim.getStrength();
            }
        }
        return new Influence(0);
    }

    public Influence getNationalStrength(Nation nation) {
        if (nation == null) return new Influence(0);
        Influence nationalStrength = new Influence(0);
        for (Claim claim : getActiveClaims()) {
            if (claim.getNation() == nation) {
                nationalStrength = nationalStrength.add(claim.getStrength());
            }
        }
        return nationalStrength;
    }

    public Influence getNationalStrength() {
        HashMap<Nation, Influence> nationalClaimStrengths = new HashMap<>();
        for (Claim claim : getActiveClaims()) {
            nationalClaimStrengths.put(claim.getNation(), nationalClaimStrengths.getOrDefault(claim.getNation(), new Influence(0)).add(claim.getStrength()));
        }
        Influence selectedStrength = new Influence(0);
        for (Nation nation : nationalClaimStrengths.keySet()) {
            if (nationalClaimStrengths.get(nation).compareTo(selectedStrength) > 0) {
                selectedStrength = nationalClaimStrengths.get(nation);
            }
        }
        return selectedStrength;
    }

    public Collection<Claim> getClaims() {
        return claims;
    }

    public List<Claim> getActiveClaims() {
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

    public String getClaimList() {

        List<String> lines = new ArrayList<>();
        Nation owningNation = getOwningNation();
        if (owningNation != null) {
            for (Nation nation : Nation.getNations()) {
                Influence strength = getNationalStrength(nation);
                if (strength.getThousandths() == 0) continue;
                String effectiveString;
                if (getTile().getMap().isTutorialMap()) {
                    effectiveString = nation.toString() + ": " + String.format("%s", strength);
                } else {
                    effectiveString = nation.getEffectiveName() + ": " + String.format("%s", strength);
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

    public String displayClaims(Format format, int amount, PlayerCharacter asker) {
        Claim askerClaim;
        if (asker != null) {
            askerClaim = getClaim(asker, asker.getNation());
        } else {
            askerClaim = null;
        }
        String tinyFormat = String.format("%s/%s/%s",
                getNationalStrength(getOwningNation()),
                getUserStrength(getOwningUserId()),
                getTotalStrength().toString());
        if (format == Format.TINY) {
            return tinyFormat;
        } else if (format == Format.ONE_LINE) {
            String ownerName = getOwningUserName();
            if (!Utils.isNullOrEmpty(getOwningUserId())) {
                String oneLine = String.format("%s of %s (%s)",
                        ownerName,
                        getOwningNation().getEffectiveName(),
                        tinyFormat);
                return oneLine;
            } else {
                return "Wildnerness";
            }
        } else if (format == Format.SIMPLE) {
            if (Utils.isNullOrEmpty(getOwningUserId())) {
                return "Wildnerness";
            }
            String nationalCompetitionByColor = "";
            List<String> nationalCompetitors = new ArrayList<>();
            for (Nation nation : Nation.getNations()) {
                if (nation == getOwningNation()) continue;
                if (getNationalStrength(nation).getThousandths() > 0) {
                    nationalCompetitors.add(String.format("%s: %s",
                            nation,
                            getNationalStrength(nation)));
                }
            }
            if (!nationalCompetitors.isEmpty()) {
                nationalCompetitionByColor = "(" + String.join(", ", nationalCompetitors) + ")";
            }
            String youTag = "";
            if (askerClaim != null && askerClaim.getInvestedStrength().getThousandths() > 0) {
                youTag = String.format("*(%s)*", askerClaim.toString());
            }
            String simple = String.format("**%s: %s** %s\n" +
                            "%s: %s %s",
                    getOwningNationName(),
                    getNationalStrength(),
                    nationalCompetitionByColor,
                    getOwningUserName(),
                    getOwnerStrength(),
                    youTag);
            return simple;
        } else if (format == Format.COMPETITIVE) {
            if (Utils.isNullOrEmpty(getOwningUserId())) {
                return "Wildnerness";
            }
            String nationalCompetition = "";
            List<String> nationalCompetitors = new ArrayList<>();
            for (Nation nation : Nation.getNations()) {
                if (nation == getOwningNation()) continue;
                if (getNationalStrength(nation).getThousandths() > 0.0) {
                    nationalCompetitors.add(String.format("%s: %s",
                            nation.getEffectiveName(),
                            getNationalStrength(nation)));
                }
            }
            if (!nationalCompetitors.isEmpty()) {
                nationalCompetition = "(" + String.join(", ", nationalCompetitors) + ")";
            }
            String nationalState = String.format("**%s: %s** %s\n",
                    getOwningNationName(),
                    getNationalStrength(),
                    nationalCompetition);
            List<Claim> claims = getActiveClaims();
            claims.sort(Comparator.comparingInt(x -> x.getStrength().getThousandths()));
            if (claims.size() > amount) {
                claims = claims.subList(0, amount);
            }
            if (askerClaim != null && !claims.contains(askerClaim) && askerClaim.isActive() && askerClaim.getNation() == getOwningNation()) {
                claims.add(getClaim(asker, asker.getNation()));
            }
            List<String> claimDisplays = claims.stream().map(x -> x.toString()).collect(Collectors.toList());
            return nationalState + String.format(String.join("\n", claimDisplays));
        } else {
            return getClaimList();
        }
    }

    public String displayClaims(Format format) {
        return displayClaims(format, 3, null);
    }

    private Influence getOwnerStrength() {
        return getUserStrength(getOwningUserId());
    }

    private PlayerCharacter getOwningUser() {
        return PlayerCharacter.get(getOwningUserId(), getTile().getMap());
    }

    private String getOwningUserName() {
        if (getOwningUser() != null) {
            return getOwningUser().getName();
        } else {
            return "Nobody";
        }
    }

    private Influence getTotalStrength() {
        Influence totalStrength = new Influence(0);
        for (Claim claim : getActiveClaims()) {
            if (claim.getStrength().getThousandths() > 0) totalStrength = totalStrength.add(claim.getStrength());
        }
        return totalStrength;
    }

    public enum Format {

        //1.5/1.8/2 //last value hidden for private servers
        TINY("%o:a%/%n:a%/%nt:a%"),

        //Hyreon (1.5/1.8) of Hyreon's Domain (1.8/3) //last tag hidden for private servers
        ONE_LINE("%o% (%o:a%/%n:a%) of %n% (%n:a%/%nt:a%)"),

        /* Can be modified by a number of nations to view
        **Hyreon's Domain: 1.8** (RED: 1.1, GREEN: 0.1) //tut/main
        **Total: 1.8** //private servers
        Hyreon: 1.5 (You: 0.02) //'you' is not there if 0
         */
        SIMPLE("%n%: %n:a% (%n2:c%: %n2:a%, %n3:c%: %n3:a%)\n" +
                "%o%: %o:a%[y: (%y%: %y:a%)]"),

        /* Can be modified by a number of players to view
        **Hyreon's Domain: 1.8** (duck home: 1.1, Green Server: 0.1) //tut/main
        **Total: 1.8** //private servers
        **1. Hyreon: 1.5** //1st (in the case of ties, both display as 1)
        2. Metel: 0.1 //2nd
        3. Terandr: 0.1 //3rd
        5. *You: 0.02* //you, or nothing if in top 3 / is 0
         */
        COMPETITIVE("%n%: %n:a% (%n2%: %n2:a%, %n3%: %n3:a%)\n" +
                "%o%: %o:a%[p2:\n%p2%: %p2:a%][p3:\n(%p3%: %p3:a%)][y*:\n*%y%: %y:a%*]"),

        /* Can be modified by a number of players to view
        **Hyreon's Domain: 1.8** (duck home: 1.1, Green Server: 0.1) //tut/main
        **Total: 1.8** //private servers
        **1. Hyreon: 1.5** //1st (in the case of ties, both display as 1)
        - Rayzr522: 0.9
        - MC_2018: 0.2
        2. Metel: 0.1 //2nd
        3. Terandr: 0.1 //3rd
        5. *You: 0.02* //you, or nothing if in top 3 / is 0
         */
        EXTENDED(""),

        /*
        Competitive, but it shows all claims, even invalid ones
         */
        COMPREHENSIVE("");

        String format;

        Format(String format) {
            this.format = format;
        }
    }
}
