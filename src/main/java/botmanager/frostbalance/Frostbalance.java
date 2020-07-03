package botmanager.frostbalance;

import botmanager.Utilities;
import botmanager.frostbalance.commands.*;
import botmanager.frostbalance.generic.FrostbalanceCommandBase;
import botmanager.frostbalance.history.RegimeData;
import botmanager.frostbalance.history.TerminationCondition;
import botmanager.generic.BotBase;
import botmanager.generic.ICommand;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//TODO change the date-time number format to something sane
public class Frostbalance extends BotBase {

    Map<Guild, List<RegimeData>> regimes = new HotMap();

    public final double DAILY_INFLUENCE_CAP = 1.00;

    public Frostbalance(String botToken, String name) {
        super(botToken, name);

        setPrefix(".");

        getJDA().getPresence().setActivity(Activity.playing(getPrefix() + "help for help!"));

        setCommands(new ICommand[] {
                new HelpCommand(this),
                new DailyRewardCommand(this),
                new InfluenceCommand(this),
                new SupportCommand(this),
                new CoupCommand(this),
                new TransferCommand(this),
                new HistoryCommand(this),
                new ImplicitInfluence(this),
                new SetGuildCommand(this)
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
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        System.out.println("CURRENT OWNER: " + getOwnerId(event.getGuild()));
        System.out.println("PLAYER LEAVING: " + event.getUser().getId());
        if (getOwnerId(event.getGuild()).equals(event.getUser().getId())) {
            System.out.println("Event fired!");
            endRegime(event.getGuild(), TerminationCondition.LEFT);
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
            guild.removeRoleFromMember(currentOwner, getOwnerRole(guild)).complete();
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

    public void startRegime(Guild guild, User user) {
        RegimeData regime = new RegimeData(guild, user.getId(), Utilities.todayAsLong());
        getRecords(guild).add(regime);

        guild.addRoleToMember(guild.getMember(user), getOwnerRole(guild)).complete();
        updateOwner(guild, user);

        logRegime(guild, regime);
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
        setUserCSVAtIndex(guild, user, 0, String.valueOf(influence + startingInfluence));
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
        return getJDA().getRolesByName("OWNER", true).get(0);
    }

    public Role getSystemRole(Guild guild) {
        return getJDA().getRolesByName("FROSTBALANCE", true).get(0);
    }
}
