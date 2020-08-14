package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import net.dv8tion.jda.api.EmbedBuilder;

public class PardonManageMenu extends Menu {

    GuildWrapper guild;
    MemberWrapper target;
    Alternative outcome = Alternative.CONFIRM;

    public PardonManageMenu(Frostbalance bot, GuildWrapper guild, UserWrapper targetUser) {
        super(bot);

        this.guild = guild;
        this.target = targetUser.getMember(guild);

        if (!target.getBanned()) {
            outcome = Alternative.FAILED;
        } else {
            target.pardon();
        }

        if (target.getUserWrapper().getGloballyBanned() && !target.getLocallyBanned()) {
            outcome = Alternative.INCOMPLETE;
        }

        menuResponses.add(new MenuResponse("\uD83C\uDF10", getMenuResponseName(Alternative.GLOBAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.GLOBAL;
                target.getUserWrapper().globalPardon();
                close(false);
            }

            @Override
            public boolean validConditions() {
                return actor.getMember(guild).getAuthority().hasAuthority(AuthorityLevel.BOT_ADMIN) &&
                        target.getUserWrapper().getGloballyBanned();
            }
        });

        menuResponses.add(new MenuResponse("✅", getMenuResponseName(Alternative.CONFIRM)) {

            @Override
            public void reactEvent() {
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
                target.ban();
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
            case CONFIRM:
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
        builder.setColor(guild.getColor());
        if (outcome.equals(Alternative.GLOBAL)) {
            builder.setTitle("**" + target.getEffectiveName() + " globally pardoned**");
            builder.setDescription("This player is no longer banned in **all** Frostbalance guilds, and all bans have been lifted.");
        } else if (outcome.equals(Alternative.CONFIRM)) {
            builder.setTitle(target.getEffectiveName() + " pardoned");
            builder.setDescription("This player has been allowed into this Frostbalance guild and can now re-join it.");
        } else if (outcome.equals(Alternative.UNDONE)) {
            builder.setTitle(target.getEffectiveName() + " banned");
            builder.setDescription("This player has been removed from this Frostbalance guild and can no longer re-join it.");
        } else if (outcome.equals(Alternative.FAILED)) {
            builder.setTitle("*" + target.getEffectiveName() + " not banned*");
            builder.setDescription("This player isn't banned from this guild.");
        } else if (outcome.equals(Alternative.INCOMPLETE)) {
            builder.setTitle("*" + target.getEffectiveName() + " has global ban*");
            builder.setDescription("This player already has a system-enforced ban on **all** guilds.");
        }
        return builder;
    }

    private enum Alternative {
        UNDONE,
        CONFIRM,
        GLOBAL,
        FAILED, //ban failed because the player wasn't banned, globally or locally.
        INCOMPLETE; //ban failed because the player has a global ban, but no local one.
    }
}
