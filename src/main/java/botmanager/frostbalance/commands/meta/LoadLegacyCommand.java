package botmanager.frostbalance.commands.meta;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase;
import botmanager.frostbalance.command.CommandContext;
import botmanager.frostbalance.menu.ConfirmationMenu;

public class LoadLegacyCommand extends FrostbalanceHybridCommandBase {

    public LoadLegacyCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "loadlegacydata"
        }, AuthorityLevel.BOT_ADMIN);
    }

    @Override
    protected void runHybrid(CommandContext eventWrapper, String... params) {
        new ConfirmationMenu(bot, () -> {
            bot.loadLegacy();
            eventWrapper.sendResponse(".csv files have been loaded and will overwrite .json data when saved.");
        },
                ":warning: Loading legacy data will overwrite any data in the new format.\n" +
                "This will affect ALL servers that run on this bot.\n" +
                "Before confirming, make sure you have created a backup! :warning:")
        .send(eventWrapper.getChannel(), eventWrapper.getAuthor());
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return null;
    }
}
