package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;

public class GetClaimsCommand extends FrostbalanceHybridCommandBase {

    public GetClaimsCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "claims",
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {

        PlayerCharacter player = PlayerCharacter.get(eventWrapper.getAuthor(), eventWrapper.getGuild());

        if (eventWrapper.getGuild() == null) {
            eventWrapper.sendResponse("no join a guild nitwit");
            return;
        }

        eventWrapper.sendResponse(player.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE, 8, player));

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return null;
    }
}
