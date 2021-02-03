package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.*;
import botmanager.frostbalance.grid.coordinate.Hex;
import botmanager.frostbalance.menu.MapMenu;

public class ViewMapCommand extends FrostbalanceGuildCommand {

    public ViewMapCommand(Frostbalance bot) {
        super(bot, new String[] {
                "map"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        ArgumentStream arguments = new ArgumentStream(params);
        Hex destination = arguments.nextCoordinate();
        if (destination == null) {
            new MapMenu(getBot(), context).send(context.getChannel(), context.getAuthor());
        } else {
            new MapMenu(getBot(), context, destination).send(context.getChannel(), context.getAuthor());
        }


    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "map** - Displays the world map (or the tutorial map)\n" +
                "**" + getBot().getPrefix() + "map LOCATION** - Displays the world map, starting with a freecam at the specified coordinates";
    }
}
