package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;

public class GetClaimsCommand extends FrostbalanceHybridCommandBase {

    public GetClaimsCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "claims",
        }, AuthorityLevel.GENERIC, Conditions.GUILD_EXISTS);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {

        PlayerCharacter player = PlayerCharacter.get(eventWrapper.getAuthor(), eventWrapper.getGuild().get());

        eventWrapper.sendResponse(String.format("Claims on %s:\n%s",
                player.getLocation().toString(),
                player.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE, 8, player)));

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "claims** - read a list of all claims on your tile";
    }
}
