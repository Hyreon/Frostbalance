package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;
import botmanager.frostbalance.menu.settings.ParentSettingsMenu;

public class SettingsCommand extends FrostbalanceGuildCommand {


    public SettingsCommand(Frostbalance bot) {
        super(bot, new String[] {
                "settings",
                "options"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    public void executeWithGuild(GuildMessageContext context, String... params) {
        String result = "";

        new ParentSettingsMenu(getBot(), context).send(context.getChannel(), context.getAuthor());

    }

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "settings** - open the settings menu";
    }
}
