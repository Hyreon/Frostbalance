package botmanager.frostbalance.commands.admin;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceCommand;
import botmanager.frostbalance.command.MessageContext;
import org.jetbrains.annotations.NotNull;

public class LoadLegacyCommand extends FrostbalanceCommand {

    public LoadLegacyCommand(Frostbalance bot) {
        super(bot, new String[] {
                "loadlegacydata"
        }, AuthorityLevel.BOT_ADMIN, ContextLevel.PUBLIC_MESSAGE);
    }

    @Override
    protected void execute(MessageContext context, String @NotNull ... params) {
        context.sendResponse("There are no recent but dated save formats.");
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return null;
    }
}
