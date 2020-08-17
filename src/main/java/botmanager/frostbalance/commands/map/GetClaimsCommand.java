package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;

public class GetClaimsCommand extends FrostbalanceGuildCommandBase {

    public GetClaimsCommand(Frostbalance bot) {
        super(bot, new String[] {
                "claims",
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {

        PlayerCharacter character = context.getPlayer().getCharacter();

        context.sendResponse(String.format("Claims on %s:\n%s",
                character.getLocation().toString(),
                character.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE, 8, character)));

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "claims** - read a list of all claims on your tile";
    }
}
