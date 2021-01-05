package botmanager.frostbalance.action.actions;

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

    public TransferAction(PlayerCharacter character, UserWrapper user, Nation inNameOf, Influence transferAmount, Hex location) {
        super(character);
        this.targetId = user.getId();
        this.inNameOf = inNameOf;
        this.transferAmount = transferAmount;
        this.location = location;
    }

    @Override
    public int moveCost() {
        return 0;
    }

    @Override
    public void doAction() throws FrostbalanceException {

        GuildWrapper guild = queue.getCharacter().getMap().getGameNetwork().guildWithAllegiance(inNameOf);
        assert guild != null;
        MemberWrapper member = queue.getCharacter().getUser().memberIn(guild);
        Claim claim = queue.getCharacter().getMap().getTile(location).getClaimData().getClaim(member);
        assert claim != null;

        new ValidationTests().throwIfAny(
                new InfluenceMinimumValidator(claim, transferAmount)
        );

        queue.getCharacter().getTile().getClaimData().transferToClaim(member, Frostbalance.bot.getMemberWrapper(targetId, guild.getId()).getPlayer(), transferAmount);

    }
}
