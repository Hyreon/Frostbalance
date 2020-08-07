package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import net.dv8tion.jda.api.entities.TextChannel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyRewardCommand extends FrostbalanceGuildCommandBase {

    SimpleDateFormat hours = new SimpleDateFormat("HH");
    
    public DailyRewardCommand(Frostbalance bot) {
        super(bot, new String[] {
                "daily"
        }, AuthorityLevel.GENERIC, Condition.PUBLIC);
    }

    @Override
    public void executeWithGuild(GuildCommandContext context, String[] params) {

        Influence gain = context.getMember().gainDailyInfluence();

        if (gain.getValue() > 0) {
            Utilities.sendGuildMessage((TextChannel) context.getChannel(), context.getJDAMember().getEffectiveName() + ", your influence increases in " + context.getGuild().getName());

            Influence influence = context.getMember().getInfluence();
            Utilities.sendPrivateMessage(context.getJDAUser(), "You now have **" + String.format("%s", influence) + "** influence in **" + context.getJDAGuild().getName() + "**.");
        } else {
            int hrsDelay = (24 - Integer.parseInt(hours.format(new Date())));
            context.sendResponse(context.getJDAMember().getEffectiveName() + ", try again at midnight EST "
                    + "(around " + hrsDelay + " hour" + (hrsDelay > 1 ? "s" : "") + ").");
        }
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "**" + bot.getPrefix() + "daily** - gives you all the influence you can get today, instantly";
        } else {
            return null;
        }
    }
}
