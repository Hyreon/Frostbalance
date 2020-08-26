package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;

public abstract class FrostbalanceGuildCommand extends FrostbalanceCommand {

    public FrostbalanceGuildCommand(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel, ContextLevel contextLevel) {
        super(bot, keywords, authorityLevel, contextLevel);
    }

    public void execute(MessageContext context, String[] params) {
        if (context.hasGuild()) {
            executeWithGuild(new GuildMessageContext(context), params);
        } else {
            context.sendResponse("This command only works if you have a default guild set for DM. Set it with `.guild GUILD`.");
        }
    }

    protected abstract void executeWithGuild(GuildMessageContext context, String... params);

    /**
     * Gets the public info of a thing. This is predefined to actually use the internal info command
     * and wrap around it, not showing anything the user doesn't have authority to see.
     * @param context the context of the info gotten
     * @return
     */
    public String getInfo(MessageContext context) {
        String info = super.getInfo(context);
        if (!context.hasGuild()) return null;
        else return info;
    }

}
