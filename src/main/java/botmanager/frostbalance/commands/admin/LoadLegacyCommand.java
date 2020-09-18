package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceCommand;
import botmanager.frostbalance.menu.input.ConfirmationMenu;

public class LoadLegacyCommand extends FrostbalanceCommand {

    public LoadLegacyCommand(Frostbalance bot) {
        super(bot, new String[] {
                "loadlegacydata"
        }, AuthorityLevel.BOT_ADMIN, ContextLevel.PUBLIC_MESSAGE);
    }

    @Override
    protected void execute(MessageContext context, String... params) {
        new ConfirmationMenu(getBot(), context, () -> {
            getBot().loadLegacy();
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
