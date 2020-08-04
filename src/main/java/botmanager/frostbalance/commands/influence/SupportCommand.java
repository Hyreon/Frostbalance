package botmanager.frostbalance.commands.influence;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceCommandBase;
import botmanager.frostbalance.command.FrostbalanceSplitCommandBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

/**
 *
 * @author MC_2018 <mc2018.git@gmail.com>
 */
public class SupportCommand extends FrostbalanceSplitCommandBase {

    private static final double PRIVATE_RATE = 0.5;

    public SupportCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "support",
                bot.getPrefix() + "s"
        }, AuthorityLevel.GENERIC, FrostbalanceCommandBase.Conditions.GUILD_EXISTS);
    }
    
    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        Influence balance;
        Influence amount;

        balance = bot.getUserInfluence(event.getMember());
        
        words = message.split(" ");
        
        if (words.length < 2) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + publicInfo(AuthorityLevel.GENERIC));
            return;
        }
        
        try {
            amount = new Influence(words[words.length - 1]);
            
            if (balance.compareTo(amount) < 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You can't offer that much support. You will instead offer all of your support.");
                amount = balance;
            } else if (amount.getValue() <= 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You have to give *some* support if you're running this command.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + publicInfo(AuthorityLevel.GENERIC));
            return;
        }
        
        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(event.getGuild(), name);
        
        if (id == null) {
            Utilities.sendGuildMessage(event.getChannel(), "Couldn't find user '" + name + "'.");
            return;
        }
        bot.changeUserInfluence(event.getMember(), amount.negate());
        bot.changeUserInfluence(event.getGuild().getMemberById(id), amount);

        event.getMessage().delete().complete();
        
        Utilities.sendGuildMessage(event.getChannel(),
                event.getMember().getEffectiveName() + " has supported "
                + event.getGuild().getMemberById(id).getEffectiveName()
                + ", giving them some influence.");

        if (amount.getValue() != 0) {
            Utilities.sendPrivateMessage(event.getGuild().getMemberById(id).getUser(),
                    event.getMember().getEffectiveName() + " has supported you, giving you " + String.format("%s", amount) + " influence in " +
                            event.getGuild().getName() + ".");
        }
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        String result;
        Influence userInfluence;
        Influence amount;

        Guild guild = bot.getUserDefaultGuild(event.getAuthor());

        if (guild == null) {
            result = "You need to set a default guild to transfer influence.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        userInfluence = bot.getUserInfluence(bot.getUserDefaultGuild(event.getAuthor()), event.getAuthor());

        words = message.split(" ");

        if (words.length < 2) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + privateInfo(AuthorityLevel.GENERIC));
            return;
        }

        try {
            amount = new Influence(words[words.length - 1]);

            if (userInfluence.compareTo(amount) < 0) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You can't offer that much support. You will instead offer all of your support.");
                amount = userInfluence;
            } else if (amount.getValue() <= 0) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You have to give *some* support if you're running this command.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + privateInfo(AuthorityLevel.GENERIC));
            return;
        }

        name = Utilities.combineArrayStopAtIndex(words, words.length - 1);
        id = Utilities.findUserId(guild, name);

        if (id == null) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Couldn't find user '" + name + "'.");
            return;
        }

        bot.changeUserInfluence(guild, event.getAuthor(), amount.negate());
        bot.changeUserInfluence(guild.getMemberById(id), new Influence(amount.getValue() * PRIVATE_RATE));

        Utilities.sendPrivateMessage(event.getAuthor(),
                "Your private support of "
                        + guild.getMemberById(id).getEffectiveName()
                        + " has been noted, giving them half of that influence. (" +
                        String.format("%s", new Influence(amount.getValue() * PRIVATE_RATE)) + ", -" +
                        String.format("%s", amount) + ")");

        if (amount.getValue() != 0) {
            Utilities.sendPrivateMessage(guild.getMemberById(id).getUser(),
                    guild.getMemberById(event.getAuthor().getId()).getEffectiveName() + " has supported you, giving you " + String.format("%s", new Influence(amount.getValue() * PRIVATE_RATE)) + " influence in " +
                            guild.getName() + ".");
        }
    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "__s__upport USER AMOUNT** - gives your influence to someone else (don't @ them)";
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "__s__upport USER AMOUNT** - gives your influence to someone else (don't @ them); only 50% of what you send is used";
    }

}
