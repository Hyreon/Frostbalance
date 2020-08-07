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
        }, AuthorityLevel.GENERIC, Condition.GUILD_EXISTS);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {

        PlayerCharacter player = PlayerCharacter.get(context.getJDAUser(), context.getJDAGuild());
        WorldMap map = WorldMap.get(context.getJDAGuild());
        Hex destination;

        if (params.length < 3) {
            new MapMenu(bot, map, PlayerCharacter.get(context.getJDAUser().getId(), map)).send(context.getChannel(), context.getJDAUser());
        } else {
            try {
                destination = new Hex(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
                new MapMenu(bot, map, PlayerCharacter.get(context.getJDAUser().getId(), map), destination).send(context.getChannel(), context.getJDAUser());
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
