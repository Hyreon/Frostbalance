package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.AuthorityLevel;
import net.dv8tion.jda.api.EmbedBuilder;

public class BanManageMenu extends Menu {

    GuildWrapper guild;
    MemberWrapper target;
    Alternative outcome = Alternative.LOCAL;

    public BanManageMenu(Frostbalance bot, GuildWrapper guild, UserWrapper targetUser) {
        super(bot);

        this.guild = guild;
        this.target = targetUser.memberIn(guild);

        if (target.getBanned()) {
            outcome = Alternative.FAILED;
        } else {
            target.setLocallyBanned(true);
        }

        if (target.getUserWrapper().getGloballyBanned() && !target.getLocallyBanned()) {
            outcome = Alternative.INCOMPLETE;
        }

        menuResponses.add(new MenuResponse("\uD83C\uDF10", getMenuResponseName(Alternative.GLOBAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.GLOBAL;
                target.getUserWrapper().globalBan();
                target.pardon();
                close(false);
            }

            @Override
            public boolean validConditions() {
                return actor.memberIn(guild).getAuthority().hasAuthority(AuthorityLevel.BOT_ADMIN) &&
                        target.getUserWrapper().getGloballyBanned();
            }
        });

        menuResponses.add(new MenuResponse("✅", getMenuResponseName(Alternative.LOCAL)) {

            @Override
            public void reactEvent() {
                outcome = Alternative.LOCAL;
                target.ban();
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
                target.pardon();
                close(false);
            }

            @Override
            public boolean validConditions() {
                return true;
            }
        });


    }

    private String getMenuResponseName(Alternative outcome) {
        switch (outcome) {
            case GLOBAL:
                switch (this.outcome) {
                    default:
                        return "Globally ban instead";
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
        builder.setColor(guild.getColor());
        if (outcome.equals(Alternative.GLOBAL)) {
            builder.setTitle("**" + target.getEffectiveName() + " globally banned**");
            builder.setDescription("This player has been instead removed from **all** Frostbalance guilds, and will be immediately banned when entering any one.");
        } else if (outcome.equals(Alternative.LOCAL)) {
            builder.setTitle(target.getEffectiveName() + " banned");
            builder.setDescription("This player has been removed from this Frostbalance guild and can no longer re-join it.");
        } else if (outcome.equals(Alternative.UNDONE)) {
            builder.setTitle(target.getEffectiveName() + " kicked");
            builder.setDescription("This player has been removed from this Frostbalance guild, but is free to re-join when desired.");
        } else if (outcome.equals(Alternative.FAILED)) {
            builder.setTitle("*" + target.getEffectiveName() + " already banned*");
            if (target.getUserWrapper().getGloballyBanned()) {
                builder.setDescription("This player already has a system-enforced ban on **all** guilds, in addition to an existing ban for this guild.");
            } else {
                builder.setDescription("This player already has a system-enforced ban on this guild.");
            }
        } else if (outcome.equals(Alternative.INCOMPLETE)) {
            builder.setTitle("*" + target.getEffectiveName() + " has global ban*");
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
