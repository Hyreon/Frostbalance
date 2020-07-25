package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.generic.AuthorityLevel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class BanManageMenu extends Menu {

    Guild guild;
    User target;
    String knownName;
    Alternative outcome = Alternative.LOCAL;

    public BanManageMenu(Frostbalance bot, Guild guild, User target) {
        super(bot);

        this.guild = guild;
        this.target = target;

        if (guild.getMember(target) != null) {
            knownName = guild.getMember(target).getEffectiveName();
        } else {
            knownName = target.getName();
        }

        if (bot.isBanned(guild, target)) {
            outcome = Alternative.FAILED;
        } else {
            bot.banUser(guild, target);
        }

        if (bot.isGloballyBanned(target) && !bot.isLocallyBanned(guild, target)) {
            outcome = Alternative.INCOMPLETE;
        }

        menuResponses.add(new MenuResponse("\uD83C\uDF10", getMenuResponseName(Alternative.GLOBAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.GLOBAL;
                bot.globallyBanUser(target);
                bot.pardonUser(guild, target);
                close(false);
            }

            @Override
            public boolean validConditions() {
                return bot.getAuthority(guild, getActor()).hasAuthority(AuthorityLevel.BOT_ADMIN) &&
                        !bot.isGloballyBanned(target);
            }
        });

        menuResponses.add(new MenuResponse("✅", getMenuResponseName(Alternative.LOCAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.LOCAL;
                bot.banUser(guild, target);
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
                bot.pardonUser(guild, target);
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
                        return "Make ban global";
                }
            case LOCAL:
                switch (this.outcome) {
                    case INCOMPLETE:
                        return "Add local ban";
                    default:
                        return "Pin and confirm";
                }
            case UNDONE:
                switch (this.outcome) {
                    case INCOMPLETE:
                        return "Do not add local ban";
                    case FAILED:
                        return "Undo local ban";
                    default:
                        return "Undo ban (kick instead)";
                }
        }
        return "Huh?";
    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(bot.getGuildColor(guild));
        if (outcome.equals(Alternative.GLOBAL)) {
            builder.setTitle("**" + knownName + " globally banned**");
            builder.setDescription("This player has been removed from **all** Frostbalance guilds, and will be immediately banned when entering any other one.");
        } else if (outcome.equals(Alternative.LOCAL)) {
            builder.setTitle(knownName + " banned");
            builder.setDescription("This player has been removed from this Frostbalance guild and can no longer re-join it.");
        } else if (outcome.equals(Alternative.UNDONE)) {
            builder.setTitle(knownName + " kicked");
            builder.setDescription("This player has been removed from this Frostbalance guild, but is free to re-join when desired.");
        } else if (outcome.equals(Alternative.FAILED)) {
            builder.setTitle("*" + knownName + " already banned*");
            if (bot.isGloballyBanned(target)) {
                builder.setDescription("This player already has a system-enforced ban on **all** guilds, in addition to an existing ban for this guild.");
            } else {
                builder.setDescription("This player already has a system-enforced ban on this guild.");
            }
        } else if (outcome.equals(Alternative.INCOMPLETE)) {
            builder.setTitle("*" + knownName + " has global ban*");
            builder.setDescription("This player already has a system-enforced ban on **all** guilds. You can add a local ban in case this global ban is lifted.");
        }
        return builder;
    }

    private enum Alternative {
        UNDONE,
        LOCAL,
        GLOBAL,
        FAILED, //ban failed because the player already has a local ban. they may also be globally banned.
        INCOMPLETE; //ban failed because the player has a global ban, but no local one.
    }
}
