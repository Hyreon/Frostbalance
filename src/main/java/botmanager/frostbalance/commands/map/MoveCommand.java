package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;

public class MoveCommand extends FrostbalanceHybridCommandBase {

    public MoveCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "move",
        }, AuthorityLevel.GENERIC, Condition.GUILD_EXISTS);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String... params) {

        if (eventWrapper.getGuild() == null) {
            eventWrapper.sendResponse("You need to have a default guild set with `.guild` in order to move on its map.");
            return;
        }

        PlayerCharacter player = PlayerCharacter.get(eventWrapper.getAuthor(), eventWrapper.getGuild().get());
        Hex destination;

        if (params.length < 3) {
            eventWrapper.sendResponse(info(eventWrapper.getAuthority(), eventWrapper.isPublic()));
            return;
        }
        try {
            destination = new Hex(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
        } catch (NumberFormatException e) {
            eventWrapper.sendResponse("One or more of these numbers aren't really numbers.");
            return;
        }

        player.setDestination(destination);
        eventWrapper.sendResponse(eventWrapper.getMember().get().getEffectiveName() + " is now headed towards " + destination + ", and will arrive in "
        + player.getTravelTime() + ".");

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "move X Y Z** - Queue your character to move to the destination";
    }
}
