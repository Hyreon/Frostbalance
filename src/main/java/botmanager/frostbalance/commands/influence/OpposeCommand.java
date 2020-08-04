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

public class OpposeCommand extends FrostbalanceSplitCommandBase {

    private static final double PRIVATE_RATE = 0.35;

    public OpposeCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "oppose",
                bot.getPrefix() + "o"
        }, AuthorityLevel.GENERIC, FrostbalanceCommandBase.Conditions.GUILD_EXISTS);
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        Influence balance, amount;

        balance = bot.getUserInfluence(event.getMember());

        words = message.split(" ");

        if (words.length < 2) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + publicInfo(AuthorityLevel.GENERIC));
            return;
        }

        try {
            amount = new Influence(words[words.length - 1]);

            if (balance.compareTo(amount) < 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You can't oppose with that much influence. You will instead oppose with all your influence.");
                amount = balance;
            } else if (amount.getValue() <= 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You have to use *some* influence if you're running this command.");
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

        if (bot.getUserInfluence(event.getGuild().getMemberById(id)).compareTo(amount) < 0) {
            Utilities.sendGuildMessage(event.getChannel(), "The target user doesn't have enough influence to match. Removing all influence.");
            amount = bot.getUserInfluence(event.getGuild().getMemberById(id));
        }

        if (!event.getMember().equals(event.getGuild().getMemberById(id))) { //if the player is hurting themselves, only drain the one time.
            bot.changeUserInfluence(event.getMember(), amount.negate());
        }

        bot.changeUserInfluence(event.getGuild().getMemberById(id), amount.negate());

        event.getMessage().delete().complete();

        Utilities.sendGuildMessage(event.getChannel(),
                event.getMember().getEffectiveName() + " has opposed "
                        + event.getGuild().getMemberById(id).getEffectiveName()
                        + ", reducing their influence.");

        if (amount.getValue() != 0) {
            Utilities.sendPrivateMessage(event.getGuild().getMemberById(id).getUser(),
                    event.getMember().getEffectiveName() + " has opposed you, reducing your influence by " + String.format("%s", amount) + " in " +
                            event.getGuild().getName() + ".");
        }
    }

    @Override
    public void runPrivate(PrivateMessageReceivedEvent event, String message) {
        String[] words;
        String id;
        String name;
        String result;
        Influence balance, amount;

        Guild guild = bot.getUserDefaultGuild(event.getAuthor());

        if (guild == null) {
            result = "You need to set a default guild to anonymously oppose players.";
            Utilities.sendPrivateMessage(event.getAuthor(), result);
            return;
        }

        balance = bot.getUserInfluence(bot.getUserDefaultGuild(event.getAuthor()), event.getAuthor());

        words = message.split(" ");

        if (words.length < 2) {
            Utilities.sendPrivateMessage(event.getAuthor(), "Proper format: " + privateInfo(AuthorityLevel.GENERIC));
            return;
        }

        try {
            amount = new Influence(words[words.length - 1]);

            if (balance.compareTo(amount) < 0) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You can't oppose with that much influence. You will instead oppose with all your influence.");
                amount = balance;
            } else if (amount.getValue() <= 0) {
                Utilities.sendPrivateMessage(event.getAuthor(), "You have to use *some* influence if you're running this command.");
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

        if (bot.getUserInfluence(guild.getMemberById(id)).compareTo(amount.applyModifier(PRIVATE_RATE)) < 0) {
            Utilities.sendPrivateMessage(event.getAuthor(), "The target user doesn't have enough influence to match. Removing all influence.");
            amount = bot.getUserInfluence(guild.getMemberById(id)).applyModifier(1 / PRIVATE_RATE);
        }

        bot.changeUserInfluence(guild, event.getAuthor(), amount.negate());

        if (!guild.getMember(event.getAuthor()).equals(guild.getMemberById(id))) { //if the player is hurting themselves, only drain the one time.
            bot.changeUserInfluence(guild.getMemberById(id), amount.negate().applyModifier(PRIVATE_RATE));

            Utilities.sendPrivateMessage(event.getAuthor(),
                    "Your anonymous smear of "
                            + guild.getMemberById(id).getEffectiveName()
                            + " has been noted, removing 35% of that influence. (" +
                            String.format("%s", amount.applyModifier(PRIVATE_RATE) + ", -" +
                            String.format("%s", amount) + ")"));

            if (amount.getValue() != 0) {
                Utilities.sendPrivateMessage(guild.getMemberById(id).getUser(),
                        "You have been smeared! You have lost " + String.format("%s", amount.applyModifier(PRIVATE_RATE)) + " influence in " +
                                guild.getName() + ".");
            }
        } else {

            Utilities.sendPrivateMessage(event.getAuthor(),
                    "Per your request, have lost " + amount + " influence for no good reason in " + guild.getName() + ".");

        }
    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "__o__ppose USER AMOUNT** - hurts your influence and someone else's by some amount (don't @ them)";
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "__o__ppose USER AMOUNT** - hurts your influence and someone else's (don't @ them) anonymously; only 35% of what you send is used";
    }
}
