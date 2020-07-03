package botmanager.frostbalance.commands;

import botmanager.Utilities;
import botmanager.frostbalance.generic.FrostbalanceHybridCommandBase;
import botmanager.generic.BotBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class InfluenceCommand extends FrostbalanceHybridCommandBase {

    public InfluenceCommand(BotBase bot) {
        super(bot,
                new String[] {bot.getPrefix() + "influence"}
                );
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String id;
        String result, privateResult;

        id = event.getAuthor().getId();
        
        if (message.length() > 0) {
            result = "If you want to find the influence of a different player, you must ask them.\n"
                + "Your influence has been sent to you via PM.";
        } else {
            result = "Your influence has been sent to you via PM.";
        }

        Member member = event.getGuild().getMemberById(id);
        double influence = bot.getUserInfluence(member);

        if (influence <= 0 && message.length() == 0) {
            privateResult = "You have *no* influence in **" + event.getGuild().getName() + "**.";
        } else {
            privateResult = "You have **" + String.format("%.3f", influence) + "** influence in **" + event.getGuild().getName() + "**.";
        }
        
        Utilities.sendGuildMessage(event.getChannel(), result);
        Utilities.sendPrivateMessage(event.getAuthor(), privateResult);
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String result;
        String[] words;

        words = message.split(" ");

        Guild userGuild = bot.getUserDefaultGuild(event.getAuthor());

        if (userGuild == null || (words.length >= 1 && words[0].equalsIgnoreCase("all"))) {

            result = "Influence in all guilds:" + "\n";

            for (Guild guild : event.getJDA().getGuilds()) {
                if (guild.getMember(event.getAuthor()) == null) {
                    continue; //this user isn't in this guild.
                }
                result += "**" + guild.getName() + "**: " + String.format("%.3f", bot.getUserInfluence(guild, event.getAuthor())) + "\n";
            }

        } else {

            result = "You have **" + String.format("%.3f", bot.getUserInfluence(userGuild, event.getAuthor())) + "** influence in **" + userGuild.getName() + "**.";

        }

        Utilities.sendPrivateMessage(event.getAuthor(), result);
    }

    @Override
    public String info() {
        return ""
                + "**" + bot.getPrefix() + "influence** - sends your influence on this server via PM";
    }
    
}
