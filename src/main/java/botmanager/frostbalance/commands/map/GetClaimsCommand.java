package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Player;
import botmanager.frostbalance.command.*;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.coordinate.Hex;

public class GetClaimsCommand extends FrostbalanceGuildCommand {

    public GetClaimsCommand(Frostbalance bot) {
        super(bot, new String[] {
                "claims",
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        Player player = context.getPlayer();

        ArgumentStream arguments = new ArgumentStream(params);
        Hex coordinate = arguments.nextCoordinate();
        if (coordinate == null) {
            coordinate = player.getCharacter().getLocation();
        }

        context.sendResponse(String.format("Claims on %s:\n%s",
                coordinate.toString(),
                context.getGameNetwork().getWorldMap().getTile(coordinate)
                        .getClaimData().displayClaims(ClaimData.Format.COMPETITIVE, 8, player)));

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "claims** - read a list of all claims on your tile";
    }
}
