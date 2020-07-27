package botmanager.frostbalance.grid;

import botmanager.frostbalance.Nation;

/**
 * A claim on a tile.
 * Each user can have one claim on a tile per guild.
 * Claims can also be revoked by a leader, reducing the strength to 0. However this
 * can always be undone by that guilds' current leader.
 */
public class Claim {

    Tile tile;
    PlayerCharacter player;
    Nation nation;
    Double strength;
    Double evictionStrength = 0.0;

    Claim(Tile tile, PlayerCharacter player, Nation nation, Double strength) {
        this.tile = tile;
        this.player = player;
        this.nation = nation;
        this.strength = strength;
        tile.addClaim(this);
    }

    /**
     * Tests if a claim overlaps another - has the same tile, user and nation.
     * This is done before adding a claim, throwing an exception;
     * as such, this method should never return true for an in-progress game.
     * @param claim The claim to test against
     * @return True if the claims overlap.
     */
    public boolean overlaps(Claim claim) {
        return this.tile.equals(claim.tile) &&
                this.player.equals(claim.player) &&
                this.nation.equals(claim.nation);
    }

    /**
     * Tests if a claim's parameters are already extant.
     * @return True if the claims overlap.
     */
    public boolean overlaps(Tile tile, PlayerCharacter player, Nation nation) {
        return this.tile.equals(tile) &&
                this.player.equals(player) &&
                this.nation.equals(nation);
    }

    public void add(Double strength) {
        this.strength += strength;
    }

    public double reduce(Double amount) {
        amount = Math.min(strength, amount);
        strength -= amount;
        evictionStrength -= amount;
        return amount;
    }

    public Nation getNation() {
        return nation;
    }

    public Double getStrength() {
        return strength - evictionStrength;
    }

    public Double getInvestedStrength() {
        return strength;
    }

    public PlayerCharacter getPlayer() {
        return player;
    }

    public boolean isActive() {
        return ownerIsInNation();
    }

    private boolean ownerIsInNation() {
        return player.getNation() == nation;
    }

    /**
     * Reduce the strength of a claim.
     * Any player can do this to their own claims at no cost, but with no refund.
     */
    public double transferToClaim(PlayerCharacter player, Nation nation, Double amount) {
        Claim claim = tile.getClaim(player, nation);
        if (claim == null) {
            return 0.0;
        }
        amount = reduce(amount);
        claim.add(amount);
        return amount;

    }
}
