package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.generic.GenericMessageReceivedEventWrapper;
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

        WorldMap map = WorldMap.get(eventWrapper.getGuild());
        new MapMenu(bot, map, PlayerCharacter.get(eventWrapper.getAuthor(), map)).send(eventWrapper.getChannel(), eventWrapper.getAuthor());

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "map** - Gets a world map that allows you to move around";
    }
}
