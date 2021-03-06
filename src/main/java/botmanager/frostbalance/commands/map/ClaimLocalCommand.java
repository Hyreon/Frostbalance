package botmanager.frostbalance.commands.map;

import botmanager.frostbalance.CooldownException;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.Nation;
import botmanager.frostbalance.command.*;
import botmanager.frostbalance.grid.ClaimData;
import botmanager.frostbalance.grid.PlayerCharacter;
import botmanager.frostbalance.menu.AllegianceMenu;
import botmanager.frostbalance.menu.input.ConfirmationMenu;
import botmanager.frostbalance.menu.response.MenuResponse;

public class ClaimLocalCommand extends FrostbalanceGuildCommand {

    public ClaimLocalCommand(Frostbalance bot) {
        super(bot, new String[] {
                "claimhere",
                "c"
        }, AuthorityLevel.GENERIC, ContextLevel.ANY);
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

        } else {

            Influence finalAmount = amount;

            Nation nation = context.getGuild().getNation();

            new ConfirmationMenu(getBot(), context, () -> {
                context.getMember().adjustInfluence(finalAmount.negate());
                character.getTile().getClaimData().addClaim(context.getMember(), finalAmount);

                context.sendResponse("You have added " + String.format("%s", finalAmount) + " to this nations' claim on this tile.\n" +
                        character.getTile().getClaimData().displayClaims(ClaimData.Format.COMPETITIVE));
            }, "Your allegiance isn't to the nation " + context.getGuild().getName() +
                    ", so you this claim won't be active. Are you sure? " +
                    "If you want your claim to be active, go to the Discord Server **" +
                    context.getGameNetwork().guildWithAllegiance(allegiance).getName() + "** and make your claim there.") {

                public ConfirmationMenu addAllegianceOption() {
                    menuResponses.add(new MenuResponse(nation.getEmoji(), "Set allegiance to " + context.getGameNetwork().guildWithAllegiance(nation).getName() + " instead") {

                        @Override
                        public boolean isValid() {
                            return true;
                        }

                        @Override
                        public void reactEvent() {
                            try {
                                context.getPlayer().setAllegiance(nation);
                                context.sendResponse("Your allegiance has been moved to this server. Try making a claim again.");
                            } catch (CooldownException e) {
                                context.sendResponse(e.getMessage());
                            }
                            close(true);
                        }
                    });
                    return this;
                }

            }.addAllegianceOption()
                    .sendOnCondition(allegiance != nation);



        }
    }

    @Override
    protected String info(AuthorityLevel authorityLevel, boolean isPublic) {
        return "**" + getBot().getPrefix() + "__c__laimhere AMOUNT** - claim the map tile you are on, for your nation, spending influence to do so";
    }
}
