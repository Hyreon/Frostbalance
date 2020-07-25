package botmanager.frostbalance;

import botmanager.Utilities;
import botmanager.frostbalance.commands.*;
import botmanager.frostbalance.commands.admin.*;
import botmanager.frostbalance.commands.meta.*;
import botmanager.frostbalance.generic.AuthorityLevel;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.RegimeData;
import botmanager.frostbalance.history.TerminationCondition;
import botmanager.frostbalance.menu.Menu;
import botmanager.generic.BotBase;
import botmanager.generic.ICommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.Guild.Ban;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.HierarchyException;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;

public class Frostbalance extends BotBase {

    private static final String BAN_MESSAGE = "You have been banned system-wide by a staff member. Either you have violated Discord's TOS or you have been warned before about some violation of Frostbalance rules. If you believe this is in error, get in touch with a staff member.";
    Map<Guild, List<RegimeData>> regimes = new HotMap<>();

    public final double DAILY_INFLUENCE_CAP = 1.00;
    private List<Menu> activeMenus = new ArrayList<>();

    public Frostbalance(String botToken, String name) {
        super(botToken, name);

        setPrefix(".");

        getJDA().getPresence().setActivity(Activity.of(Activity.ActivityType.DEFAULT,getPrefix() + "help for help!"));

        setCommands(new ICommand[] {
                new HelpCommand(this),
                new ImplicitInfluence(this),
                new DailyRewardCommand(this),
                new GetInfluenceCommand(this),
                new SupportCommand(this),
                new OpposeCommand(this),
                new CheckCommand(this),
                new CoupCommand(this),
                new TransferCommand(this),
                new HistoryCommand(this),
                new SetGuildCommand(this),
                new InterveneCommand(this),
                new AdjustCommand(this),
                new SystemBanCommand(this),
                new SystemPardonCommand(this),
                new FlagCommand(this),
        });
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        for (ICommand command : getCommands()) {
            command.run(event);
        }
    }
    
    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        for (ICommand command : getCommands()) {
            command.run(event);
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        Menu targetMenu = null;
        for (Menu menu : getActiveMenus()) {
            if (event.getUser().equals(menu.getActor())) {
                if (menu.getMessage().getId().equals(event.getMessageId())) {
                    targetMenu = menu;
                    break;
                }
            }
        }
        if (targetMenu != null) {
            targetMenu.applyResponse(event.getReactionEmote());
        }
    }

    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
        Menu targetMenu = null;
        for (Menu menu : getActiveMenus()) {
            if (event.getUser().equals(menu.getActor())) {
                if (menu.getMessage().getId().equals(event.getMessageId())) {
                    targetMenu = menu;
                    break;
                }
            }
        }
        if (targetMenu != null) {
            targetMenu.applyResponse(event.getReactionEmote());
        }
    }

    //TODO add the effect to the icon
    @Override
    public void onGuildUpdateIcon(GuildUpdateIconEvent event) {
        event.getNewIconUrl();
        //new GuildManagerImpl(event.getGuild()).setIcon(null).queue();
    }

    private List<Menu> getActiveMenus() {
        return activeMenus;
    }

    public void addMenu(Menu menu) {
        activeMenus.add(menu);
    }

    public void removeMenu(Menu menu) {
        activeMenus.remove(menu);
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        System.out.println("PLAYER LEAVING: " + event.getUser().getId());
        if (getOwnerId(event.getGuild()).equals(event.getUser().getId())) {
            System.out.println("Leader left, making a note here");
            endRegime(event.getGuild(), TerminationCondition.LEFT);
        }
        try {
            event.getGuild().retrieveBan(event.getUser()).complete(); //verify this player was banned and didn't just leave
            if (hasDiplomatStatus(event.getUser())
                    && !isBanned(event.getGuild(), event.getUser())
                    && getDebugFlags(event.getGuild()).contains(OptionFlag.MAIN)) {
                event.getGuild().unban(event.getUser()).complete();
                Utilities.sendGuildMessage(event.getGuild().getDefaultChannel(),
                        event.getUser().getName() + " has been unbanned because they are the leader of a main server.");
            }
        } catch (ErrorResponseException e) {
            //nothing, there was no ban.
            //TODO ask for permission, not forgiveness
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        System.out.println("PLAYER JOINING: " + event.getUser().getId());
        if (isBanned(event.getGuild(), event.getUser())) {
            System.out.println("Found a banned player, banning them once again");
            event.getGuild().ban(event.getUser(), 0, BAN_MESSAGE).queue();
        }
    }

    public String getUserCSVAtIndex(Guild guild, User user, int index) {

        String guildId;
        if (guild == null) {
            guildId = "global";
        } else {
            guildId = guild.getId();
        }

        File file = new File("data/" + getName() + "/" + guildId + "/" + user.getId() + ".csv");

        if (!file.exists()) {
            return "";
        }

        return Utilities.getCSVValueAtIndex(Utilities.read(file), index);
    }

    public void setUserCSVAtIndex(Guild guild, User user, int index, String newValue) {

        String guildId;
        if (guild == null) {
            guildId = "global";
        } else {
            guildId = guild.getId();
        }

        File file = new File("data/" + getName() + "/" + guildId + "/" + user.getId() + ".csv");
        String data = Utilities.read(file);
        String[] originalValues = data.split(",");
        String[] newValues;

        if (originalValues.length > index) {
            newValues = data.split(",");
        } else {
            newValues = new String[index + 1];
            System.arraycopy(originalValues, 0, newValues, 0, originalValues.length);

            for (int i = originalValues.length; i < newValues.length; i++) {
                newValues[i] = "";
            }
        }
        
        newValues[index] = newValue;
        Utilities.write(file, Utilities.buildCSV(newValues));
    }

    private List<String> getAdminIds() {
        return Utilities.readLines(new File("data/" + getName() + "/staff.csv"));
    }

    public void loadRecords(Guild guild) {

        List<String> info = Utilities.readLines(new File("data/" + getName() + "/" + guild.getId() + "/history.csv"));
        if (info != null && !info.isEmpty()) {
            for (String line : info) {
                System.out.println(line);

                if (line.isEmpty()) {
                    regimes.getOrDefault(guild, new ArrayList<>());
                    continue;
                }

                String rulerId = Utilities.getCSVValueAtIndex(line, 0);
                String lastKnownUserName = Utilities.getCSVValueAtIndex(line, 1);
                long startDay;
                try {
                    startDay = Long.parseLong(Utilities.getCSVValueAtIndex(line, 2));
                } catch (NumberFormatException e) {
                    startDay = 0;
                }
                long endDay;
                try {
                    endDay = Long.parseLong(Utilities.getCSVValueAtIndex(line, 3));
                } catch (NumberFormatException e) {
                    endDay = 0;
                }
                TerminationCondition terminationCondition;
                try {
                    terminationCondition = TerminationCondition.valueOf(Utilities.getCSVValueAtIndex(line, 4));
                } catch (IllegalArgumentException | NullPointerException e) {
                    terminationCondition = TerminationCondition.UNKNOWN;
                }

                regimes.getOrDefault(guild, new ArrayList<>()).add(new RegimeData(guild, rulerId, startDay, endDay, terminationCondition, lastKnownUserName));

            }
        }

    }

    public boolean hasBeenForciblyRemoved(Member member) {
        List<RegimeData> relevantRegimes = getRecords(member.getGuild());
        try {
            RegimeData lastRegime = relevantRegimes.get(relevantRegimes.size() - 1);
            if (lastRegime.getTerminationCondition() == TerminationCondition.RESET && lastRegime.getUserId().equals(member.getUser().getId())) {
                return false;
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return true;
        }
    }

    public void banUser(Guild guild, User user) {

        setUserCSVAtIndex(guild, user, 1, Boolean.TRUE.toString());
        try {
            guild.ban(user, 0).queue();
        } catch (HierarchyException e) {
            System.err.println("Unable to ban admin user " + user.getName() + ".");
            e.printStackTrace();
        }

    }

    /**
     *
     * @param guild
     * @param user
     * @return Whether the user had a ban from the server when pardoned.
     */
    public boolean pardonUser(Guild guild, User user) {

        setUserCSVAtIndex(guild, user, 1, Boolean.FALSE.toString());
        try {
            guild.unban(user).queue();
        } catch (ErrorResponseException e) {
            return false;
        }
        return true;

    }

    public void globallyBanUser(User user) {

        Utilities.append(new File("data/" + getName() + "/global/bans.csv"), user.getId());
        for (Guild guild : getJDA().getGuilds()) {
            if (guild.isMember(user)) {
                try {
                    guild.ban(user, 0).queue();
                } catch (HierarchyException e) {
                    System.err.println("Unable to fully ban user " + user.getName() + " because they have admin privileges in some servers!");
                    e.printStackTrace();
                }
            }
        }

    }

    public boolean globallyPardonUser(User user) {

        File file = new File("data/" + getName() + "/global/bans.csv");

        boolean found = false;
        List<String> validBans = new ArrayList<>();

        for (String line : Utilities.readLines(file)) {
            if (!line.equals(user.getId())) {
                validBans.add(line);
            } else {
                found = true;

                for (Guild guild : getJDA().getGuilds()) {
                    try {
                        guild.unban(user).queue();
                    } catch (ErrorResponseException e) {
                        //nothing
                    }
                }

            }
        }

        Utilities.write(file, String.join("\n", validBans));

        return found;

    }

    /**
     * Returns if the player is banned from this guild.
     * @param guild The guild to check
     * @param user The user to check
     * @return Whether this user is banned from this guild, or banned globally
     */
    public boolean isBanned(Guild guild, User user) {

        return isGloballyBanned(user) || isLocallyBanned(guild, user);

    }

    /**
     * Returns if the player is banned from this guild.
     * @param guild The guild to check
     * @param user The user to check
     * @return Whether this user is banned from this guild, or banned globally
     */
    public boolean isLocallyBanned(Guild guild, User user) {

        return Boolean.parseBoolean(getUserCSVAtIndex(guild, user, 1));

    }

    /**
     * Returns if the player is globally banned.
     * This function is expensive and should not be fired often.
     * @param user The user to check
     * @return Whether this user is banned globally
     */
    public boolean isGloballyBanned(User user) {

        List<String> bannedUserIds = Utilities.readLines(new File("data/" + getName() + "/global/bans.csv"));
        for (String bannedUserId : bannedUserIds) {
            if (bannedUserId.equals(user.getId())) {
                return true;
            }
        }

        return false;

    }

    private List<RegimeData> getRecords(Guild guild) {

        if (regimes.get(guild) == null) {
            loadRecords(guild);
        }
        return regimes.getOrDefault(guild, new ArrayList<>());

    }

    public List<RegimeData> readRecords(Guild guild) {

        if (regimes.get(guild) == null) {
            loadRecords(guild);
        }
        if (regimes.get(guild) == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(regimes.get(guild));
        }

    }

    public void updateLastRegime(Guild guild, RegimeData regime) {
        Utilities.removeLine(new File("data/" + getName() + "/" + guild.getId() + "/history.csv"));
        Utilities.append(new File("data/" + getName() + "/" + guild.getId() + "/history.csv"), regime.toCSV());
    }

    public void logRegime(Guild guild, RegimeData regime) {
        Utilities.append(new File("data/" + getName() + "/" + guild.getId() + "/history.csv"), regime.toCSV());
    }

    public void endRegime(Guild guild, TerminationCondition condition) {
        List<RegimeData> regimeData = getRecords(guild);

        String currentOwnerId = getOwnerId(guild);
        Member currentOwner = getOwner(guild);

        if (currentOwner != null) {
            guild.removeRoleFromMember(currentOwner, getOwnerRole(guild)).queue();
        }

        if (currentOwnerId != null && !currentOwnerId.isEmpty()) {
            System.out.println("Ending active regime of " + currentOwnerId);
            try {
                int lastRegimeIndex = regimeData.size() - 1;
                regimeData.get(lastRegimeIndex).end(condition);
                updateLastRegime(guild, regimeData.get(lastRegimeIndex));
            } catch (IndexOutOfBoundsException e) {
                System.err.println("Index out of bounds when trying to adjust the last regime! The history data may be lost.");
                System.err.println("Creating a fragmented history.");
                RegimeData regime = new RegimeData(guild, currentOwnerId);
                regime.end(condition);
                regimeData.add(regime);
                logRegime(guild, regime);
            }

            removeOwner(guild);
        }
    }

    /**
     * Returns true if the player is a leader in a main server.
     * @param user The user in question
     * @return True if a guild can be found where this player is the same as the owner of that server.
     */
    public boolean hasDiplomatStatus(User user) {
        for (Guild guild : getJDA().getGuilds()) {
            if (getDebugFlags(guild).contains(OptionFlag.MAIN) && getOwner(guild).getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    public void startRegime(Guild guild, User user) {
        RegimeData regime = new RegimeData(guild, user.getId(), Utilities.todayAsLong());
        getRecords(guild).add(regime);

        guild.addRoleToMember(guild.getMember(user), getOwnerRole(guild)).queue();
        updateOwner(guild, user);

        logRegime(guild, regime);
    }

    public Collection<OptionFlag> getDebugFlags(Guild guild) {
        Collection<OptionFlag> debugFlags = new ArrayList<OptionFlag>();
        List<String> flags = Utilities.readLines(new File("data/" + getName() + "/" + guild.getId() + "/flags.csv"));
        for (String flag : flags) {
            debugFlags.add(OptionFlag.valueOf(flag));
        }
        return debugFlags;
    }

    /**
     * Flips a flag for a guild.
     * @param guild
     * @param toggledFlag
     * @return Whether the debug flag got turned on (TRUE) or off (FALSE)
     */
    public boolean flipFlag(Guild guild, OptionFlag toggledFlag) {
        if (getDebugFlags(guild).contains(toggledFlag)) {
            removeDebugFlag(guild, toggledFlag);
            return false;
        } else {
            for (OptionFlag previousFlag : getDebugFlags(guild)) {
                if (previousFlag.isExclusiveWith(toggledFlag)) {
                    removeDebugFlag(guild, previousFlag);
                }
            }
            addDebugFlag(guild, toggledFlag);
            return true;
        }
    }

    public void addDebugFlag(Guild guild, OptionFlag debugFlag) {
        File file = new File("data/" + getName() + "/" + guild.getId() + "/flags.csv");
        Utilities.append(file, debugFlag.toString());
    }

    public void removeDebugFlag(Guild guild, OptionFlag debugFlag) {
        File file = new File("data/" + getName() + "/" + guild.getId() + "/flags.csv");
        List<String> lines = Utilities.readLines(file);

        Iterator<String> i = lines.iterator();
        while (i.hasNext()) {
            String line = i.next();
            if (debugFlag.equals(OptionFlag.valueOf(line))) {
                i.remove();
                break;
            }
        }

        Utilities.write(file, "");

        for (String line : lines) {
            Utilities.append(file, line);
        }

    }

    public String getOwnerId(Guild guild) {
        String info = Utilities.read(new File("data/" + getName() + "/" + guild.getId() + "/owner.csv"));
        return Utilities.getCSVValueAtIndex(info, 0);
    }

    public Member getOwner(Guild guild) {
        try {
            String info = Utilities.read(new File("data/" + getName() + "/" + guild.getId() + "/owner.csv"));
            return guild.getMember(getJDA().getUserById(Utilities.getCSVValueAtIndex(info, 0)));
        } catch (NullPointerException | IllegalArgumentException e) {
            return null;
        }
    }

    public void updateOwner(Guild guild, User user) {
        Utilities.write(new File("data/" + getName() + "/" + guild.getId() + "/owner.csv"), user.getId());
    }

    public void removeOwner(Guild guild) {
        Utilities.removeFile(new File("data/" + getName() + "/" + guild.getId() + "/owner.csv"));
    }

    public void changeUserInfluence(Guild guild, User user, double influence) {
        double startingInfluence = getUserInfluence(guild, user);
        double newInfluence = influence + startingInfluence;
        newInfluence = Math.round(newInfluence * 1000.0) / 1000.0;
        if (newInfluence < 0) {
            newInfluence = 0;
        }
        setUserCSVAtIndex(guild, user, 0, String.valueOf(newInfluence));
    }

    public void changeUserInfluence(Member member, double influence) {
        changeUserInfluence(member.getGuild(), member.getUser(), influence);
    }

    public double gainDailyInfluence(Member member, double influenceGained) {
        if (getUserLastDaily(member) != Utilities.todayAsLong()) { //new day
            setUserLastDaily(member, Utilities.todayAsLong());
            setUserDailyAmount(member, influenceGained);
            changeUserInfluence(member, influenceGained);
            return influenceGained;
        } else if (getUserDailyAmount(member) + influenceGained <= DAILY_INFLUENCE_CAP) { //cap doesn't affect anything
            setUserDailyAmount(member, getUserDailyAmount(member) + influenceGained);
            changeUserInfluence(member, influenceGained);
            return influenceGained;
        } else { //influence gained is over the cap
            influenceGained = DAILY_INFLUENCE_CAP - getUserDailyAmount(member); //set it to the cap before doing anything
            setUserDailyAmount(member, getUserDailyAmount(member) + influenceGained);
            changeUserInfluence(member, influenceGained);
            return influenceGained;
        }
    }

    public double getUserInfluence(Guild guild, User user) {
        try {
            return Double.parseDouble(getUserCSVAtIndex(guild, user, 0));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getUserInfluence(Member member) {
        return getUserInfluence(member.getGuild(), member.getUser());
    }

    public double getUserDailyAmount(Guild guild, User user) {
        try {
            return Double.parseDouble(getUserCSVAtIndex(guild, user, 3));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double getUserDailyAmount(Member member) {
        return getUserDailyAmount(member.getGuild(), member.getUser());
    }

    public void setUserDailyAmount(Guild guild, User user, double amount) {
        setUserCSVAtIndex(guild, user, 3, String.valueOf(amount));
    }

    public void setUserDailyAmount(Member member, double amount) {
        setUserDailyAmount(member.getGuild(), member.getUser(), amount);
    }

    public long getUserLastDaily(Guild guild, User user) {
        try {
            return Integer.parseInt(getUserCSVAtIndex(guild, user, 2));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public long getUserLastDaily(Member member) {
        return getUserLastDaily(member.getGuild(), member.getUser());
    }

    public void setUserLastDaily(Guild guild, User user, long date) {
        setUserCSVAtIndex(guild, user, 2, String.valueOf(date));
    }

    public void setUserLastDaily(Member member, long date) {
        setUserLastDaily(member.getGuild(), member.getUser(), date);
    }

    public void resetUserDefaultGuild(User user) {
        setUserCSVAtIndex(null, user, 0, "");
    }

    public void setUserDefaultGuild(User user, Guild guild) {
        setUserCSVAtIndex(null, user, 0, guild.getId());
    }

    public Guild getUserDefaultGuild(User user) {
        try {
            return getJDA().getGuildById(getUserCSVAtIndex(null, user, 0));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public FrostbalanceCommandBase[] getCommands() {
        ICommand[] commands = super.getCommands();
        FrostbalanceCommandBase[] newCommands = new FrostbalanceCommandBase[commands.length];
        
        for (int i = 0; i < commands.length; i++) {
            newCommands[i] = (FrostbalanceCommandBase) commands[i];
        }
        
        return newCommands;
    }

    public Role getOwnerRole(Guild guild) {
        try {
            return guild.getRolesByName("LEADER", true).get(0);
        } catch (IndexOutOfBoundsException e) {
            System.err.println(guild.getName() + " doesn't have a valid owner role!");
            return null;
        }
    }

    public Role getSystemRole(Guild guild) {
        try {
            return guild.getRolesByName("FROSTBALANCE", true).get(0);
        } catch (IndexOutOfBoundsException e) {
            System.err.println(guild.getName() + " doesn't have a valid frostbalance role!");
            return null;
        }
    }

    public Role getForeignOwnerRole(Guild inGuild, Guild fromGuild) {

        if (!getDebugFlags(inGuild).contains(OptionFlag.MAIN)) {
            return null;
        } else if (!getDebugFlags(fromGuild).contains(OptionFlag.MAIN)) {
            return null;
        }

        Collection<OptionFlag> foreignOptions = getDebugFlags(fromGuild);
        if (foreignOptions.contains(OptionFlag.RED)) {
            try {
                return inGuild.getRolesByName("RED LEADER", true).get(0);
            } catch (IndexOutOfBoundsException e) {
                System.err.println(inGuild.getName() + " doesn't have a valid red owner role!");
                return null;
            }
        } else if (foreignOptions.contains(OptionFlag.GREEN)) {
            try {
                return inGuild.getRolesByName("GREEN LEADER", true).get(0);
            } catch (IndexOutOfBoundsException e) {
                System.err.println(inGuild.getName() + " doesn't have a valid green owner role!");
                return null;
            }
        } else if (foreignOptions.contains(OptionFlag.BLUE)) {
            try {
                return inGuild.getRolesByName("BLUE LEADER", true).get(0);
            } catch (IndexOutOfBoundsException e) {
                System.err.println(inGuild.getName() + " doesn't have a valid blue owner role!");
                return null;
            }
        }

        throw new IllegalStateException("Main server exists without a color scheme!");
    }

    public boolean hasSystemRoleEverywhere(User user) {
        for (Guild guild : getJDA().getGuilds()) {
            if (guild.getMember(user) == null || guild.getMember(user).getRoles().contains(getSystemRole(guild))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs a soft reset on a guild. This will set reset all player roles and lift all bans.
     * In the future, it will also reset the server icon and name.
     * It will *not* reset player data about influence, leader history, channels or their conversations.
     * @param guild The guild to reset.
     */
    public void softReset(Guild guild) {
        List<Role> roles = guild.getRoles();
        for (Role role : roles) {
            if (!getSystemRole(guild).equals(role) && !getOwnerRole(guild).equals(role)) {
                role.delete();
            }
        }
        //TODO don't unban players who are under a global ban.
        for (Ban ban : guild.retrieveBanList().complete()) {
            guild.unban(ban.getUser());
        }
    }

    //FIXME perform functions different than the soft reset.
    /**
     * Performs a hard reset on a guild. This will set reset all player roles and lift all bans,
     * reset the server name and icon, delete all stored data about a server and its members, and delete
     * all channels and their conversations, leaving only a general channel.
     * It will *not* reset player data about influence, leader history, channels or their conversations.
     * @param guild The guild to reset.
     */
    public void hardReset(Guild guild) {
        softReset(guild);
    }

    /**
     * Returns how much authority a user has in a given context.
     * @param guild The server they are operating in
     * @param user The user that is operating
     * @return The authority level of the user
     */
    public AuthorityLevel getAuthority(Guild guild, User user) {

        if (this.getJDA().getSelfUser().getId().equals(user.getId())) {
            return AuthorityLevel.BOT;
        } else if (getAdminIds().contains(user.getId())) {
            return AuthorityLevel.BOT_ADMIN;
        } else if (guild == null) {
            return AuthorityLevel.GENERIC;
        }

        if (guild.getOwner().getUser().getId().equals(user.getId())) {
            return AuthorityLevel.GUILD_OWNER;
        } else if (guild.getMember(user).getRoles().contains(getSystemRole(guild))) {
            return AuthorityLevel.GUILD_ADMIN;
        } else if (guild.getMember(user).getRoles().contains(getOwnerRole(guild))) {
            return AuthorityLevel.SERVER_LEADER;
        } else if (guild.getMember(user).hasPermission(Permission.ADMINISTRATOR)) {
            return AuthorityLevel.SERVER_ADMIN;
        } else {
            return AuthorityLevel.GENERIC;
        }
    }

    public AuthorityLevel getAuthority(Member member) {
        return getAuthority(member.getGuild(), member.getUser());
    }

    public Color getGuildColor(Guild guild) {
        Collection<OptionFlag> flags = getDebugFlags(guild);
        if (flags.contains(OptionFlag.RED)) {
            return Color.RED;
        } else if (flags.contains(OptionFlag.GREEN)) {
            return Color.GREEN;
        } else if (flags.contains(OptionFlag.BLUE)) {
            return Color.BLUE;
        } else {
            return Color.LIGHT_GRAY;
        }
    }
}
