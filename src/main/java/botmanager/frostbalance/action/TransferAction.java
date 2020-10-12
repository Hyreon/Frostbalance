package botmanager.frostbalance.action;

import botmanager.frostbalance.*;
import botmanager.frostbalance.checks.FrostbalanceException;
import botmanager.frostbalance.checks.InfluenceMinimumValidator;
import botmanager.frostbalance.checks.ValidationTests;
import botmanager.frostbalance.grid.Claim;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

public class TransferAction extends Action {

    String targetId;
    Nation inNameOf;
    Influence transferAmount;
    Hex location;

    public TransferAction(UserWrapper user, Nation inNameOf, Influence transferAmount, Hex location) {
        this.targetId = user.getId();
        this.inNameOf = inNameOf;
        this.transferAmount = transferAmount;
        this.location = location;
    }

    @Override
    public void doAction(PlayerCharacter playerCharacter) throws FrostbalanceException {

        GuildWrapper guild = playerCharacter.getMap().getGameNetwork().guildWithAllegiance(inNameOf);
        assert guild != null;
        MemberWrapper member = playerCharacter.getUser().memberIn(guild);
        Claim claim = playerCharacter.getMap().getTile(location).getClaimData().getClaim(member);
        assert claim != null;

        new ValidationTests().throwIfAny(
                new InfluenceMinimumValidator(claim, transferAmount)
        );

        playerCharacter.getTile().getClaimData().transferToClaim(member, Frostbalance.bot.getMemberWrapper(targetId, guild.getId()).getPlayer(), transferAmount);

    }
}
