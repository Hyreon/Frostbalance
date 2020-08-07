package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;

public class MoveCommand extends FrostbalanceGuildCommandBase {

    public MoveCommand(Frostbalance bot) {
        super(bot, new String[] {
                "move",
        }, AuthorityLevel.GENERIC, Condition.GUILD_EXISTS);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {

        if (context.getJDAGuild() == null) {
            context.sendResponse("You need to have a default guild set with `.guild` in order to move on its map.");
            return;
        }

        PlayerCharacter player = PlayerCharacter.get(context.getJDAUser(), context.getJDAGuild());
        Hex destination;

        if (params.length < 3) {
            context.sendResponse(info(context.getAuthority(), context.isPublic()));
            return;
        }
        try {
            destination = new Hex(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
        } catch (NumberFormatException e) {
            context.sendResponse("One or more of these numbers aren't really numbers.");
            return;
        }

        player.setDestination(destination);
        context.sendResponse(context.getJDAMember().getEffectiveName() + " is now headed towards " + destination + ", and will arrive in "
        + player.getTravelTime() + ".");

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "move X Y Z** - Queue your character to move to the destination";
    }
}
