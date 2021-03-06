package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.Player;
import org.jetbrains.annotations.NotNull;

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

    private Influence promised;
    private Influence strength;
    Influence evictionStrength = new Influence(0);

    Claim(ClaimData tileClaimData, Player player, Nation nation, Influence strength) {
        this.claimData = tileClaimData;
        this.userId = player.getUserWrapper().getId();
        this.nation = nation;
        this.strength = strength;
        claimData.addClaim(this);
    }

    Claim(ClaimData tileClaimData, Player player, Nation nation, Influence strength, boolean actual) {
        this.claimData = tileClaimData;
        this.userId = player.getUserWrapper().getId();
        this.nation = nation;
        if (actual) {
            this.strength = strength;
        } else {
            this.strength = Influence.none();
            this.promised = strength;
        }
        claimData.addClaim(this);
    }

    Claim(PlayerCharacter character, Nation nation, Influence strength) {
        this.claimData = character.getTile().getClaimData();
        this.userId = character.getUserId();
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
    public boolean overlaps(ClaimData claimData, Player player, Nation nation) {
        return this.claimData.equals(claimData) &&
                this.userId.equals(player.getUserWrapper().getId()) &&
                this.nation.equals(nation);
    }

    public void add(Influence strength) {
        this.strength = this.strength.add(strength);
    }

    /**
     * Reduce the strength of a claim.
     * Any player can do this to their own claims at no cost, but with no refund.
     * @param refunded Whether or not the influence returned is refunded (if so, then eviction strength
     *               is not modified); does NOT take into account what nation it comes from or to.
     * @return The amount of influence actually reduced; this might be lower if there wasn't enough influence
     * to transfer.
     */
    public Influence reduce(Influence amount, boolean refunded) {
        if (refunded) {
            amount = new Influence(Math.min(strength.subtract(evictionStrength).getThousandths(), amount.getThousandths()));
        } else {
            amount = new Influence(Math.min(strength.getThousandths(), amount.getThousandths()));
        }
        strength = strength.subtract(amount);
        if (!refunded) {
            evictionStrength = evictionStrength.subtract(amount);
            if (evictionStrength.isNegative()) evictionStrength = Influence.none();
        }
        return amount;
    }

    public void evict() {
        evictionStrength = strength;
    }

    public void unevict() {
        evictionStrength = Influence.none();
    }

    public Nation getNation() {
        return nation;
    }

    public Influence getStrength() {
        if (strength == null) strength = Influence.none();
        return isActive() ? strength.subtract(evictionStrength) : Influence.none();
    }

    public Influence getInvestedStrength() {
        return strength;
    }

    public Player getPlayer() {
        return Frostbalance.bot.getUserWrapper(userId).playerIn(claimData.getTile().getMap().getGameNetwork());
    }

    public boolean isActive() {
        return ownerIsInNation();
    }

    private boolean ownerIsInNation() {
        return getPlayer().getAllegiance() == nation;
    }

    /**
     * Transfer this claim to another in the same nation.
     * @return the amount of influence actually transferred.
     */
    public Influence transferToClaim(Player player, Influence amount) {
        Claim claim = claimData.getClaim(player, nation);
        if (claim == null) {
            claim = new Claim(claimData, player, nation, Influence.none());
        }
        amount = reduce(amount, true);
        claim.add(amount);
        return amount;

    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        if (!isActive()) {
            return "~~" + ownerName() + ": " + getInvestedStrength() + "~~";
        }
        if (!getStrength().equals(getInvestedStrength())) {
            return ownerName() + ": ~~" + getInvestedStrength() + "~~ " + getStrength();
        }
        return ownerName() + ": " + getInvestedStrength();
    }

    private String ownerName() {
        return Frostbalance.bot.getUserWrapper(getUserId()).getName();
    }

    @Override
    public void setParent(ClaimData parent) {
        claimData = parent;
    }

    public void addPromise(@NotNull Influence strength) {
        promised = promised.add(strength);
    }

    /**
     * Undo the promise part of a claim, the part that becomes active when a player walks over it.
     * @param amount The amount of promised territory that is being revoked from this claim
     * @return The amount actually revoked
     */
    @NotNull
    public Influence revokePromise(Influence amount) {
        Influence totalRevoked = amount;
        promised.subtract(amount);
        if (promised.isNegative()) {
            totalRevoked = amount.add(promised); //10 - 4 = 6, for 10 revoked with -4 left
            promised = Influence.none();
        }
        return totalRevoked;
    }
}
