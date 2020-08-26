package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;

public class MoveCommand extends FrostbalanceGuildCommand {

    public MoveCommand(Frostbalance bot) {
        super(bot, new String[] {
                "move",
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        PlayerCharacter character = context.getPlayer().getCharacter();
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

        character.setDestination(destination);
        context.sendResponse(context.getMember().getEffectiveName() + " is now headed towards " + destination + ", and will arrive in "
        + character.getTravelTime() + ".");

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "move X Y Z** - Queue your character to move to the destination";
    }
}
