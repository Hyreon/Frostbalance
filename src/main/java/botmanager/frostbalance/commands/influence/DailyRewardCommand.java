package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceSplitCommandBase;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */

public class DailyRewardCommand extends FrostbalanceSplitCommandBase {

    SimpleDateFormat hours = new SimpleDateFormat("HH");
    
    public DailyRewardCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "daily"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        Influence gain = bot.gainDailyInfluence(event.getMember());

        if (gain.getValue() > 0) {
            Utilities.sendGuildMessage(event.getChannel(), event.getMember().getEffectiveName() + ", your influence increases.");

            Influence influence = bot.getUserInfluence(event.getMember());
            Utilities.sendPrivateMessage(event.getAuthor(), "You now have **" + String.format("%s", influence) + "** influence in **" + event.getGuild().getName() + "**.");
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
