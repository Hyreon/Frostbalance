package botmanager.frostbalance;

import botmanager.Utilities;
import botmanager.frostbalance.data.RegimeData;
import botmanager.frostbalance.data.TerminationCondition;
import botmanager.frostbalance.grid.WorldMap;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class GuildWrapper {

    @Getter
    transient Frostbalance bot;

    @Getter
    String guildId;

    transient BufferedImage guildIcon;
    @Getter
    String lastKnownName;

    @Getter
    Optional<String> ownerId = Optional.empty();

    Optional<WorldMap> map = Optional.empty();

    Set<OptionFlag> optionFlags = new HashSet<>();
    List<RegimeData> regimes = new ArrayList<>();

    public GuildWrapper(Frostbalance bot, Guild guild) {
        this.bot = bot;
        this.guildId = guild.getId();
    }

    public String getName() {
        if (getJDA().getGuildById(guildId) != null) {
            lastKnownName = getJDA().getGuildById(guildId).getName();
        }
        return lastKnownName;
    }

    public Optional<Guild> getGuild() {
        return Optional.ofNullable(getJDA().getGuildById(guildId));
    }

    public JDA getJDA() {
        return bot.getJDA();
    }

    public Color getColor() {
        if (optionFlags.contains(OptionFlag.RED)) {
            return Color.RED;
        } else if (optionFlags.contains(OptionFlag.GREEN)) {
            return Color.GREEN;
        } else if (optionFlags.contains(OptionFlag.BLUE)) {
            return Color.BLUE;
        } else {
            return Color.LIGHT_GRAY;
        }
    }

    /**
     * Returns a modifiable clone of this server's records.
     */
    public List<RegimeData> readRecords() {
        return new ArrayList<>(regimes);
    }

    public boolean hasBeenForciblyRemoved(String userId) {
        return (regimes.size() > 0 &&
                regimes.get(regimes.size() - 1).getUserId().equals(userId) &&
                regimes.get(regimes.size() - 1).getTerminationCondition().equals(TerminationCondition.RESET));
    }

    public void doCoup(User user) {
        endRegime(TerminationCondition.COUP);
        startRegime(user);
    }

    private void endRegime(TerminationCondition condition) {

        if (!getGuild().isPresent()) {
            throw new IllegalStateException("Tried to end the regime of a server the bot isn't in!");
        }

        getLeaderAsMember().ifPresent(
                leader -> getGuild().get().removeRoleFromMember(leader, getLeaderRole()).queue()
        );

        if (ownerId.isPresent()) {
            System.out.println("Ending active regime of " + ownerId);
            try {
                int lastRegimeIndex = regimes.size() - 1;
                regimes.get(lastRegimeIndex).end(condition);
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Index out of bounds when trying to adjust the last regime! The history data may be lost.");
                System.err.println("Creating a fragmented history.");
                RegimeData regime = new RegimeData(this, ownerId.get());
                regime.end(condition);
                regimes.add(regime);
            }

            ownerId = Optional.empty();
        }
    }

    private void startRegime(User user) {
        RegimeData regime = new RegimeData(this, user.getId(), Utilities.todayAsLong());
        regimes.add(regime);

        getGuild().get().addRoleToMember(user.getId(), getLeaderRole()).queue();
        ownerId = Optional.of(user.getId());
    }

    private Role getLeaderRole() {
        try {
            return getGuild().get().getRolesByName("LEADER", true).get(0);
        } catch (IndexOutOfBoundsException e) {
            System.err.println(getName() + " doesn't have a valid leader role! Attempting to create...");
            Role role = getGuild().get().createRole()
                    .setColor(getColor())
                    .setName("Leader")
                    .setPermissions(Permission.ADMINISTRATOR)
                    .setHoisted(true)
                    .complete();
            getGuild().get()
                    .modifyRolePositions()
                    .selectPosition(role)
                    .moveTo(getSystemRole().getPosition() - 1)
                    .queue();
            return role;
        }
    }

    private Role getSystemRole() {
        try {
            return getGuild().get().getRolesByName("FROSTBALANCE", true).get(0);
        } catch (IndexOutOfBoundsException indexException) {
            System.err.println(getName() + " doesn't have a valid system role! Attempting to create...");
            try {
                Role role = getGuild().get().createRole()
                        .setColor(getColor())
                        .setName("Leader")
                        .setPermissions(Permission.ADMINISTRATOR)
                        .setHoisted(true)
                        .complete();
                getGuild().get().addRoleToMember(getJDA().getSelfUser().getId(), role).queue();
                getGuild().get()
                        .modifyRolePositions()
                        .selectPosition(role)
                        .moveTo(getGuild().get().getRoles().size() - 1)
                        .queue();
                return role;
            } catch (ErrorResponseException | IllegalArgumentException | InsufficientPermissionException | IllegalStateException errorException) {
                getGuild().get().getDefaultChannel().sendMessage(getGuild().get().getOwner().getAsMention() + " I cannot function because there is no 'Frostbalance' role. This server is dead without one.");
                errorException.addSuppressed(indexException);
                throw errorException;
            }
        }
    }

    private Optional<Member> getLeaderAsMember() {
        return ownerId.flatMap(ownerId -> bot.getMemberWrapper(ownerId, guildId).getMember());
    }
}
