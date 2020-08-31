package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.command.*;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.menu.AllegianceMenu;

public class ClaimTileCommand extends FrostbalanceGuildCommand {

    public ClaimTileCommand(Frostbalance bot) {
        super(bot, new String[] {
                "claim"
        }, AuthorityLevel.GENERIC, ContextLevel.NATIONAL_MESSAGE);
    }

    @Override
    protected void executeWithGuild(GuildMessageContext context, String... params) {

        ArgumentStream arguments = new ArgumentStream(params);

        Influence amount;
        Influence balance = context.getMember().getInfluence();
        PlayerCharacter character = context.getPlayer().getCharacter();
        Nation allegiance = context.getPlayer().getAllegiance();

        if (params.length < 1 || params[0].isEmpty()) {
            context.sendResponse(getInfo(context));
            return;
        }

        try {

            arguments.exhaust(1);
            try {
                amount = arguments.nextInfluence(true);
                if (amount == null) {
                    amount = Influence.none();
                }
            } catch (NumberFormatException e) {
                context.sendResponse(getInfo(context));
                return;
            }

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

        if ((context.getGameNetwork().hasMultipleNations()) && allegiance == null) {

            new AllegianceMenu(getBot(), context).send(context.getChannel(), context.getAuthor());

        } else if (allegiance != context.getGuild().getNation()) {

            context.sendResponse("You're in the wrong server for this!");

        } else {

            context.getMember().adjustInfluence(amount.negate());
            character.getTile().getClaimData().addClaim(context.getPlayer(), amount);

            context.sendResponse("You have added " + String.format("%s", amount) + " to your nations' claim on this tile.\n" +
                    character.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE));

        }
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "claim AMOUNT** - claim the map tile you are on, for your nation, spending influence to do so";
    }
}
