package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.grid.Hex;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.menu.MapMenu;

public class ViewMapCommand extends FrostbalanceGuildCommandBase {

    public ViewMapCommand(Frostbalance bot) {
        super(bot, new String[] {
                "map"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {

        WorldMap map = bot.getGameNetwork(context.getGuild().getId()).getWorldMap();
        PlayerCharacter character = context.getAuthor().playerIn(context.getGameNetwork()).getCharacter();
        Hex destination;

        if (params.length < 3) {
            new MapMenu(bot, map, character).send(context.getChannel(), context.getAuthor());
        } else {
            try {
                destination = new Hex(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
                new MapMenu(bot, map, character, destination).send(context.getChannel(), context.getAuthor());
            } catch (NumberFormatException e) {
                context.sendResponse("One or more of these numbers aren't really numbers.");
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
