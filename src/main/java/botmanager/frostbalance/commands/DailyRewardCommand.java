package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */

public class DailyRewardCommand extends FrostbalanceSplitCommandBase {

    SimpleDateFormat hours = new SimpleDateFormat("HH");
    
    public DailyRewardCommand(BotBase bot) {
        super(bot, new String[] {
                bot.getPrefix() + "daily"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        double gain = bot.gainDailyInfluence(event.getMember(), 1);

        if (gain > 0) {
            Utilities.sendGuildMessage(event.getChannel(), event.getMember().getEffectiveName() + ", your influence increases.");

            double influence = bot.getUserInfluence(event.getMember());
            Utilities.sendPrivateMessage(event.getAuthor(), "You now have **" + String.format("%.3f", influence) + "** influence in **" + event.getGuild().getName() + "**.");
        } else {
            int hrsDelay = (24 - Integer.parseInt(hours.format(new Date())));
            Utilities.sendGuildMessage(event.getChannel(), event.getMember().getEffectiveName() + ", try again at midnight EST "
                    + "(around " + hrsDelay + " hour" + (hrsDelay > 1 ? "s" : "") + ").");
        }
    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "daily** - gives you all the influence you can get today, instantly";
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }

}
