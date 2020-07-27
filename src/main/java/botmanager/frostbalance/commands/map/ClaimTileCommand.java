package botmanager.frostbalance.commands.map;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.grid.WorldMap;
import botmanager.frostbalance.menu.AllegianceMenu;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public class ClaimTileCommand extends FrostbalanceSplitCommandBase {

    public ClaimTileCommand(Frostbalance bot) {
        super(bot, new String[] {
                bot.getPrefix() + "claim"
        });
    }

    @Override
    public void runPublic(GuildMessageReceivedEvent event, String message) {

        String[] words = message.split(" ");
        double amount;
        double balance = bot.getUserInfluence(event.getMember());
        PlayerCharacter player = PlayerCharacter.get(event.getAuthor(), event.getGuild());
        Nation allegiance = bot.getMainAllegiance(event.getAuthor());

        if (words.length < 1 || words[0].isEmpty()) {
            Utilities.sendGuildMessage(event.getChannel(), publicInfo(AuthorityLevel.GENERIC));
        }

        try {
            amount = Double.parseDouble(words[words.length - 1]);

            if (balance < amount) {
                Utilities.sendGuildMessage(event.getChannel(), "You don't have enough influence to make this claim. You will instead use all your influence.");
                amount = balance;
            } else if (amount <= 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You can't make a claim with that little influence!");
                return;
            } else if (amount == Double.NaN) {
                Utilities.sendGuildMessage(event.getChannel(), "NOPE.");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + publicInfo(AuthorityLevel.GENERIC));
            return;
        }

        if (allegiance == null) {

            new AllegianceMenu(bot).send(event.getChannel(), event.getAuthor());

        } else if (bot.getAllegianceIn(event.getGuild()) != Nation.NONE && allegiance != bot.getAllegianceIn(event.getGuild())) {

            Utilities.sendGuildMessage(event.getChannel(), "You're in the wrong server for this!");

        } else {

            bot.changeUserInfluence(event.getMember(), -amount);
            WorldMap.get(event.getGuild()).getTile(player.getLocation()).addClaim(player, amount);

            Utilities.sendGuildMessage(event.getChannel(), "You have added " + amount + " to your nations' claim on this tile.");

        }

    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return null;
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }
}
