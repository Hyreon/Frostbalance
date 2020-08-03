package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.Nation;

/**
 * A claim on a tile.
 * Each user can have one claim on a tile per guild.
 * Claims can also be revoked by a leader, reducing the strength to 0. However this
 * can always be undone by that guilds' current leader.
 */
public class Claim implements Containable<ClaimData> {

    transient ClaimData claimData;
    String userId;
    Nation nation;

    Influence strength;
    Influence evictionStrength = new Influence(0);

    Claim(PlayerCharacter player, Nation nation, Influence strength) {
        this.claimData = player.getTile().getClaimData();
        this.userId = player.getUserId();
        this.nation = nation;
        this.strength = strength;
        claimData.addClaim(this);
    }

    /**
     * Tests if a claim overlaps another - has the same tile, user and nation.
     * This is done before adding a claim, throwing an exception;
     * as such, this method should never return true for an in-progress game.
     * @param claim The claim to test against
     * @return True if the claims overlap.
     */
    public boolean overlaps(Claim claim) {
        return this.claimData.equals(claim.claimData) &&
                this.userId.equals(claim.userId) &&
                this.nation.equals(claim.nation);
    }

    /**
     * Tests if a claim's parameters are already extant.
     * @return True if the claims overlap.
     */
    public boolean overlaps(ClaimData claimData, PlayerCharacter player, Nation nation) {
        return this.claimData.equals(claimData) &&
                this.userId.equals(player.getUserId()) &&
                this.nation.equals(nation);
    }

    public void add(Influence strength) {
        this.strength = this.strength.add(strength);
    }

    /**
     * Reduce the strength of a claim.
     * Any player can do this to their own claims at no cost, but with no refund.
     * @return The amount of influence actually reduced; this might be lower if there wasn't enough influence
     * to transfer.
     */
    public Influence reduce(Influence amount) {
        amount = new Influence(Math.min(strength.getThousandths(), amount.getThousandths()));
        strength = strength.subtract(amount);
        evictionStrength = evictionStrength.subtract(amount);
        return amount;
    }

    public Nation getNation() {
        return nation;
    }

    public Influence getStrength() {
        return strength.subtract(evictionStrength);
    }

    public Influence getInvestedStrength() {
        return strength;
    }

    public PlayerCharacter getPlayer() {
        return PlayerCharacter.get(userId, claimData.getTile().getMap());
    }

    public boolean isActive() {
        return ownerIsInNation();
    }

    private boolean ownerIsInNation() {
        return getPlayer().getNation() == nation;
    }

    /**
     * Transfer this claim to another in the same nation.
     * @return the amount of influence actually transferred.
     */
    public Influence transferToClaim(PlayerCharacter player, Influence amount) {
        Claim claim = claimData.getClaim(player, nation);
        if (claim == null) {
            return new Influence(0);
        }
        amount = reduce(amount);
        claim.add(amount);
        return amount;

    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        if (!isActive()) {
            return "~~" + Frostbalance.bot.getUserName(getUserId()) + ": " + getStrength() + "~~";
        }
        if (!getStrength().equals(getInvestedStrength())) {
            return Frostbalance.bot.getUserName(getUserId()) + ": ~~" + getInvestedStrength() + "~~ " + getStrength();
        }
        return Frostbalance.bot.getUserName(getUserId()) + ": " + getInvestedStrength();
    }
}
