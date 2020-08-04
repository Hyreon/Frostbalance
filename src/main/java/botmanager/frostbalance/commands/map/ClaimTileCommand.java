package botmanager.frostbalance.commands.map;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceSplitCommandBase;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;
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
        Influence amount;
        Influence balance = bot.getUserInfluence(event.getMember());
        PlayerCharacter player = PlayerCharacter.get(event.getAuthor(), event.getGuild());
        Nation allegiance = bot.getMainAllegiance(event.getAuthor());

        if (words.length < 1 || words[0].isEmpty()) {
            Utilities.sendGuildMessage(event.getChannel(), publicInfo(AuthorityLevel.GENERIC));
        }

        try {
            amount = new Influence(words[words.length - 1]);

            if (balance.compareTo(amount) < 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You don't have enough influence to make this claim. You will instead use all your influence.");
                amount = balance;
            } else if (amount.getValue() <= 0) {
                Utilities.sendGuildMessage(event.getChannel(), "You can't make a claim with that little influence!");
                return;
            }
        } catch (NumberFormatException e) {
            Utilities.sendGuildMessage(event.getChannel(), "Proper format: " + publicInfo(AuthorityLevel.GENERIC));
            return;
        }

        if ((player.getMap().isMainMap() || player.getMap().isTutorialMap()) && allegiance == Nation.NONE) {

            new AllegianceMenu(bot).send(event.getChannel(), event.getAuthor());

        } else if (bot.getAllegianceIn(event.getGuild()) != Nation.NONE &&
                allegiance != bot.getAllegianceIn(event.getGuild())) {

            Utilities.sendGuildMessage(event.getChannel(), "You're in the wrong server for this!");

        } else {

            bot.changeUserInfluence(event.getMember(), amount.negate());
            player.getTile().getClaimData().addClaim(player, amount);

            Utilities.sendGuildMessage(event.getChannel(), "You have added " + String.format("%s", amount) + " to your nations' claim on this tile.\n" +
                    player.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE));

        }

    }

    @Override
    public String publicInfo(AuthorityLevel authorityLevel) {
        return "**" + bot.getPrefix() + "claim AMOUNT** - claim the map tile you are on, for your nation, spending influence to do so";
    }

    @Override
    public String privateInfo(AuthorityLevel authorityLevel) {
        return null;
    }
}
