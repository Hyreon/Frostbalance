package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.menu.MapMenu;

public class ViewMapCommand extends FrostbalanceHybridCommandBase {

    public ViewMapCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "map"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void runHybrid(GenericMessageReceivedEventWrapper eventWrapper, String message) {

        if (eventWrapper.getGuild() == null) {
            eventWrapper.sendResponse("You need to have a default guild set with `.guild` in order to view its map.");
            return;
        }

        String[] words = message.split(" ");
        PlayerCharacter player = PlayerCharacter.get(eventWrapper.getAuthor(), eventWrapper.getGuild());
        WorldMap map = WorldMap.get(eventWrapper.getGuild());
        Hex destination;

        if (words.length < 3) {
            new MapMenu(bot, map, PlayerCharacter.get(eventWrapper.getAuthor().getId(), map)).send(eventWrapper.getChannel(), eventWrapper.getAuthor());
        } else {
            try {
                destination = new Hex(Integer.parseInt(words[0]), Integer.parseInt(words[1]), Integer.parseInt(words[2]));
                new MapMenu(bot, map, PlayerCharacter.get(eventWrapper.getAuthor().getId(), map), destination).send(eventWrapper.getChannel(), eventWrapper.getAuthor());
            } catch (NumberFormatException e) {
                eventWrapper.sendResponse("One or more of these numbers aren't really numbers.");
                return;
            }
        }


    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "map** - Displays the world map (or the tutorial map)\n" +
                "**" + bot.getPrefix() + "map X Y Z** - Displays the world map, starting with a freecam at the specified coordinates";
    }
}
