package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class PardonManageMenu extends Menu {

    Guild guild;
    User target;
    String knownName;
    Alternative outcome = Alternative.LOCAL;

    public PardonManageMenu(Frostbalance bot, Guild guild, User target) {
        super(bot);

        this.guild = guild;
        this.target = target;

        if (guild.getMember(target) != null) {
            knownName = guild.getMember(target).getEffectiveName();
        } else {
            knownName = target.getName();
        }

        if (!bot.isBanned(guild, target)) {
            outcome = Alternative.FAILED;
        } else {
            bot.pardonUser(guild, target);
        }

        if (bot.isGloballyBanned(target) && !bot.isLocallyBanned(guild, target)) {
            outcome = Alternative.INCOMPLETE;
        }

        menuResponses.add(new MenuResponse("\uD83C\uDF10", getMenuResponseName(Alternative.GLOBAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.GLOBAL;
                bot.globallyPardonUser(target);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return bot.getAuthority(guild, getActor()).hasAuthority(AuthorityLevel.BOT_ADMIN) &&
                        bot.isGloballyBanned(target);
            }
        });

        menuResponses.add(new MenuResponse("✅", getMenuResponseName(Alternative.LOCAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.LOCAL;
                bot.pardonUser(guild, target);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });

        menuResponses.add(new MenuResponse("❎", getMenuResponseName(Alternative.UNDONE)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.UNDONE;
                bot.banUser(guild, target);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });


    }

    private Alternative getOutcome() {
        return outcome;
    }

    private String getMenuResponseName(Alternative outcome) {
        switch (outcome) {
            case GLOBAL:
                switch (this.outcome) {
                    default:
                        return "Globally pardon";
                }
            case LOCAL:
                switch (this.outcome) {
                    case INCOMPLETE:
                        return "Keep pardon only local";
                    default:
                        return "Pin and confirm";
                }
            case UNDONE:
                switch (this.outcome) {
                    case INCOMPLETE:
                    case FAILED:
                        return "Ban locally";
                    default:
                        return "Undo local pardon";
                }
        }
        return "Huh?";
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(bot.getGuildColor(guild));
        if (outcome.equals(Alternative.GLOBAL)) {
            builder.setTitle("**" + knownName + " globally pardoned**");
            builder.setDescription("This player has been allowed into **all** Frostbalance guilds, and all bans have been lifted.");
        } else if (outcome.equals(Alternative.LOCAL)) {
            if (bot.isGloballyBanned(target)) {
                builder.setTitle("*" + knownName + " globally banned*");
                builder.setDescription("This player remains banned from all Frostbalance servers.");
            } else {
                builder.setTitle(knownName + " pardoned");
                builder.setDescription("This player has been allowed into this Frostbalance guild and can now re-join it.");
            }
        } else if (outcome.equals(Alternative.UNDONE)) {
            builder.setTitle(knownName + " banned");
            builder.setDescription("This player has been removed from this Frostbalance guild and can no longer re-join it.");
        } else if (outcome.equals(Alternative.FAILED)) {
            builder.setTitle("*" + knownName + " not banned*");
            builder.setDescription("This player isn't banned from this guild.");
        } else if (outcome.equals(Alternative.INCOMPLETE)) {
            builder.setTitle("*" + knownName + " has global ban*");
            builder.setDescription("This player already has a system-enforced ban on **all** guilds.");
        }
        return builder;
    }

    private enum Alternative {
        UNDONE,
        LOCAL,
        GLOBAL,
        FAILED, //ban failed because the player wasn't banned, globally or locally.
        INCOMPLETE; //ban failed because the player has a global ban, but no local one.
    }
}
