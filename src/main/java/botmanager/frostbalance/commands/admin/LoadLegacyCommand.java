package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.command.FrostbalanceCommandBase;
import botmanager.frostbalance.menu.ConfirmationMenu;

public class LoadLegacyCommand extends FrostbalanceCommandBase {

    public LoadLegacyCommand(Frostbalance bot) {
        super(bot, new String[] {
                "loadlegacydata"
        }, AuthorityLevel.BOT_ADMIN);
    }

    @Override
    protected void execute(CommandContext context, String... params) {
        new ConfirmationMenu(bot, () -> {
            bot.loadLegacy();
            context.sendResponse(".csv files have been loaded and will overwrite .json data when saved.");
        },
                ":warning: Loading legacy data will overwrite any data in the new format.\n" +
                "This will affect ALL servers that run on this bot.\n" +
                "Before confirming, make sure you have created a backup! :warning:")
        .send(context.getChannel(), context.getAuthor());
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return null;
    }
}
