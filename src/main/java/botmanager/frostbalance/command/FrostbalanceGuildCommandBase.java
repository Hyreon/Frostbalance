package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;

public abstract class FrostbalanceGuildCommandBase extends FrostbalanceCommandBase {

    public FrostbalanceGuildCommandBase(Frostbalance bot, String[] keywords, AuthorityLevel authorityLevel, Condition... conditions) {
        super(bot, keywords, authorityLevel, conditions);
    }

    public void execute(CommandContext context, String[] params) {
        if (context.hasGuild()) {
            executeWithGuild(new GuildCommandContext(context), params);
        } else {
            context.sendResponse("This command only works if you have a default guild set for DM. Set it with `.guild GUILD`.");
        }
    }

    protected abstract void executeWithGuild(GuildCommandContext context, String... params);

    /**
     * Gets the public info of a thing. This is predefined to actually use the internal info command
     * and wrap around it, not showing anything the user doesn't have authority to see.
     * @param context the context of the info gotten
     * @return
     */
    public String getInfo(CommandContext context) {
        String info = super.getInfo(context);
        if (!context.hasGuild()) return null;
        else return info;
    }

}
