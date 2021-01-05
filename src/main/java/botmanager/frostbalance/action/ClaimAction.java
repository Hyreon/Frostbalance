package botmanager.frostbalance.action;

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

    public ClaimAction(Influence amount, Nation nation, Hex location) {
        this.amountToClaim = amount;
        this.inNameOf = nation;
        this.location = location;
    }

    @Override
    public void doAction(PlayerCharacter playerCharacter) throws FrostbalanceException {

        GuildWrapper guild = playerCharacter.getMap().getGameNetwork().guildWithAllegiance(inNameOf);
        MemberWrapper member = playerCharacter.getUser().memberIn(guild);

        new ValidationTests().throwIfAny(
                new LocationValidator(playerCharacter, location),
                new InfluenceMinimumValidator(member, amountToClaim)
        );

        playerCharacter.getTile().getClaimData().addClaim(member, amountToClaim);

    }
}