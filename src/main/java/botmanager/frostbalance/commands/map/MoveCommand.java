package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;

public class MoveCommand extends FrostbalanceHybridCommandBase {

    public MoveCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "move",
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {

        if (eventWrapper.getGuild() == null) {
            eventWrapper.sendResponse("You need to have a default guild set with `.guild` in order to move on its map.");
            return;
        }

        String[] words = message.split(" ");
        PlayerCharacter player = PlayerCharacter.get(eventWrapper.getAuthor(), eventWrapper.getGuild());
        Hex destination;

        if (words.length < 3) {
            eventWrapper.sendResponse(info(eventWrapper.getAuthority(), eventWrapper.isPublic()));
            return;
        }
        try {
            destination = new Hex(Integer.parseInt(words[0]), Integer.parseInt(words[1]), Integer.parseInt(words[2]));
        } catch (NumberFormatException e) {
            eventWrapper.sendResponse("One or more of these numbers aren't really numbers.");
            return;
        }

        player.setDestination(destination);
        eventWrapper.sendResponse(eventWrapper.getMember().getEffectiveName() + " is now headed towards " + destination + ", and will arrive in "
        + player.getTravelTime() + ".");

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "move X Y Z** - Queue your character to move to the destination";
    }
}
