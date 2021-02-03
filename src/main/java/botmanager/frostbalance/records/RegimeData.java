package botmanager.frostbalance.records;

import botmanager.Utilities;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.GuildWrapper;
import botmanager.frostbalance.grid.Containable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegimeData implements Containable<GuildWrapper> {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MMMM dd");

    transient public GuildWrapper guildWrapper;

    String userId;
    long startDay;
    long endDay;
    TerminationCondition terminationCondition;

    /**
     * Create a complete regime.
     * @param guildWrapper
     * @param userId
     * @param startDay
     * @param endDay
     * @param terminationCondition
     */
    public RegimeData(GuildWrapper guildWrapper, String userId, long startDay, long endDay, TerminationCondition terminationCondition) {
        this.guildWrapper = guildWrapper;
        this.userId = userId;
        this.terminationCondition = terminationCondition;
        this.startDay = startDay;
        this.endDay = endDay;
    }

    /**
     * Create a regime that has not yet ended.
     * @param guildWrapper
     * @param userId
     * @param startDay
     */
    public RegimeData(GuildWrapper guildWrapper, String userId, long startDay) {
        this(guildWrapper, userId, startDay, 0, TerminationCondition.NONE);
    }

    /**
     * Create a basic regime with only the user and their guild.
     * @param guildWrapper
     * @param userId
     */
    public RegimeData(GuildWrapper guildWrapper, String userId) {
        this(guildWrapper, userId, 0, 0, TerminationCondition.UNKNOWN);
    }

    public String getRulerDisplayName() {
        System.out.println(guildWrapper);
        System.out.println(guildWrapper.getMember(Frostbalance.bot.getUserWrapper(userId)));
        return guildWrapper.getMember(Frostbalance.bot.getUserWrapper(userId)).getEffectiveName();
    }

    public String forHumans() {
        return getRulerDisplayName() + ": " +
                (startDay != 0 ? LocalDate.ofEpochDay(startDay).format(FORMAT) : "???") + " - " +
                (endDay != 0 ? LocalDate.ofEpochDay(endDay).format(FORMAT) + " (" : "(") +
                terminationCondition.getDisplayText() + ")";
    }

    @Override
    public String toString() {
        return forHumans();
    }

    public String toCSV() {
        return String.join(",", userId, getRulerDisplayName(), String.valueOf(startDay), String.valueOf(endDay), terminationCondition.name());
    }

    public void end(TerminationCondition condition) {
        endDay = Utilities.todayAsLong();
        terminationCondition = condition;
    }

    public String getUserId() {
        return userId;
    }

    public TerminationCondition getTerminationCondition() {
        return terminationCondition;
    }

    @Override
    public void setParent(GuildWrapper parent) {
        guildWrapper = parent;
    }
}
