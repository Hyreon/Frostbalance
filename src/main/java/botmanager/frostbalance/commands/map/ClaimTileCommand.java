package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.NationColor;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.FrostbalanceGuildCommandBase;
import botmanager.frostbalance.command.GuildCommandContext;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.menu.AllegianceMenu;

public class ClaimTileCommand extends FrostbalanceGuildCommandBase {

    public ClaimTileCommand(Frostbalance bot) {
        super(bot, new String[] {
                "claim"
        }, AuthorityLevel.GENERIC);
    }

    @Override
    protected void executeWithGuild(GuildCommandContext context, String... params) {
        if (context.isPublic()) runPublic(context, String.join(" ", params));
    }

    public void runPublic(GuildCommandContext context, String message) {

        String[] words = message.split(" ");
        Influence amount;
        Influence balance = context.getMember().getInfluence();
        PlayerCharacter player = PlayerCharacter.get(context.getJDAUser(), context.getJDAGuild());
        NationColor allegiance = context.getAuthor().getAllegiance();

        if (words.length < 1 || words[0].isEmpty()) {
            context.sendResponse(getInfo(context));
            return;
        }

        try {
            amount = new Influence(words[words.length - 1]);

            if (balance.compareTo(amount) < 0) {
                context.sendResponse("You don't have enough influence to make this claim. You will instead use all your influence.");
                amount = balance;
            } else if (amount.getValue() <= 0) {
                context.sendResponse("You can't make a claim with that little influence!");
                return;
            }
        } catch (NumberFormatException e) {
            context.sendResponse("Proper format: " + getInfo(context));
            return;
        }

        if ((context.getGuild().usesNations()) && allegiance == NationColor.NONE) {

            new AllegianceMenu(bot).send(context.getChannel(), context.getAuthor());

        } else if (context.getGuild().getNationColor() != NationColor.NONE &&
                allegiance != bot.getAllegianceIn(context.getJDAGuild())) {

            context.sendResponse("You're in the wrong server for this!");

        } else {

            context.getMember().adjustInfluence(amount.negate());
            player.getTile().getClaimData().addClaim(player, amount);

            context.sendResponse("You have added " + String.format("%s", amount) + " to your nations' claim on this tile.\n" +
                    player.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE));

        }

    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + bot.getPrefix() + "claim AMOUNT** - claim the map tile you are on, for your nation, spending influence to do so";
    }
}
