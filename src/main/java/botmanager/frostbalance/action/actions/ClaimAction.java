package botmanager.frostbalance.action.actions;

import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.checks.InfluenceMinimumValidator;
import botmanager.frostbalance.checks.LocationValidator;
import botmanager.frostbalance.checks.ValidationTests;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

public class ClaimAction extends Action {

    Influence amountToClaim;
    Nation inNameOf;
    Hex location;

    public ClaimAction(PlayerCharacter character, Influence amount, Nation nation, Hex location) {
        super(character);
        this.amountToClaim = amount;
        this.inNameOf = nation;
        this.location = location;
    }

    @Override
    public int moveCost() {
        return 0;
    }

    @Override
    public void doAction() throws FrostbalanceException {

        GuildWrapper guild = queue.getCharacter().getMap().getGameNetwork().guildWithAllegiance(inNameOf);
        MemberWrapper member = queue.getCharacter().getUser().memberIn(guild);

        new ValidationTests().throwIfAny(
                new LocationValidator(queue.getCharacter(), location),
                new InfluenceMinimumValidator(member, amountToClaim)
        );

        queue.getCharacter().getTile().getClaimData().addClaim(member, amountToClaim);

    }
}
