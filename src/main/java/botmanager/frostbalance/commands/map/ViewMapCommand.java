package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
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

        Hex destination;

        if (params.length < 3) {
            new MapMenu(getBot(), context).send(context.getChannel(), context.getAuthor());
        } else {
            try {
                destination = new Hex(Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
                new MapMenu(getBot(), context, destination).send(context.getChannel(), context.getAuthor());
            } catch (NumberFormatException e) {
                context.sendResponse("One or more of these numbers aren't really numbers.");
                return;
            }
        }


    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "map** - Displays the world map (or the tutorial map)\n" +
                "**" + getBot().getPrefix() + "map X Y Z** - Displays the world map, starting with a freecam at the specified coordinates";
    }
}
