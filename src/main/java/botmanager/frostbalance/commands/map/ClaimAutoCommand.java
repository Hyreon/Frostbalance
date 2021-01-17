package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommand;
import botmanager.frostbalance.command.GuildMessageContext;

public class ClaimAutoCommand extends FrostbalanceGuildCommand {


    public ClaimAutoCommand(Frostbalance bot) {
        super(bot, new String[] {
                "autoclaim"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {
        //TODO autoclaim
        // - max influence per day
        // - max influence per tile
        // - max influence per tile per day
        // - target tile influence
        // - approach target (spend as much influence as allowed) or meet target (spend influence if target can be met)
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "autoclaim** - configure autoclaim";
    }

}
