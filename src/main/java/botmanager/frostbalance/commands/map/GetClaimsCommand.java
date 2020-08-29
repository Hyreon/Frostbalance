package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.*;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.coordinate.Hex;

public class GetClaimsCommand extends FrostbalanceGuildCommand {

    public GetClaimsCommand(Frostbalance bot) {
        super(bot, new String[] {
                "claims",
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        PlayerCharacter character = context.getPlayer().getCharacter();

        ArgumentStream arguments = new ArgumentStream(params);
        Hex coordinate = arguments.nextSpacedCoordinate();
        if (coordinate == null) {
            coordinate = character.getLocation();
        }

        context.sendResponse(String.format("Claims on %s:\n%s",
                coordinate.toString(),
                context.getGameNetwork().getWorldMap().getTile(coordinate)
                        .getClaimData().displayClaims(ClaimData.Format.COMPETITIVE, 8, character)));

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "claims** - read a list of all claims on your tile";
    }
}
