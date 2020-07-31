package botmanager.frostbalance.grid;

import botmanager.Utils;
import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCharacter extends TileObject {

    public static final long MOVEMENT_SPEED = 120000;

    static List<PlayerCharacter> cache = new ArrayList<>();

    public static PlayerCharacter get(String userId, WorldMap map) {
        if (Utils.isNullOrEmpty(userId)) return null;
        for (PlayerCharacter player : cache) {
            if (player.getUserId().equals(userId) && player.getTile().getMap().equals(map)) {
                return player;
            }
        }
        PlayerCharacter newPlayer = new PlayerCharacter(userId, map);
        cache.add(newPlayer);
        System.out.println("Creating new character for user with id " + userId);
        return newPlayer;
    }

    public static PlayerCharacter get(User user, Guild guild) {
        return get(user.getId(), WorldMap.get(guild));
    }

    transient private ScheduledFuture<?> scheduledFuture = null;

    /**
     * The user this character is tied to.
     */
    String userId;

    /**
     * The queued destination that the player is currently approaching via the fastest known route.
     */
    Hex destination;

    public PlayerCharacter(String userId, WorldMap map) {
        super(map.getTile(new Hex()));
        this.userId = userId;
    }

    /**
     * @return The user from this id, or null if the user is inaccessible.
     */
    public User getUser() {
        User user = Frostbalance.bot.getJDA().getUserById(userId);
        return user;
    }

    public String getUserId() {
        return userId;
    }

    /**
     *
     * @return The nation this player is a part of, if relevant.
     * Note the user can change this at any time.
     */
    public Nation getNation() {
        if (getMap().isMainMap() || getMap().isTutorialMap()) {
            return Frostbalance.bot.getMainAllegiance(getUser());
        } else {
            return Nation.NONE;
        }
    }


    public void setDestination(Hex destination) {
        this.destination = destination;
        updateMovement();
    }

    public void adjustDestination(Hex.Direction direction) {
        destination = getDestination().move(direction);
        updateMovement();
    }

    /**
     * Moves towards the destination in one second.
     */
    private void updateMovement() {

        if (scheduledFuture != null && !scheduledFuture.isDone()) return;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {

            if (!getLocation().equals(getDestination())) {

                Hex directions = destination.subtract(getLocation());
                Hex.Direction nextStep = directions.crawlDirection();
                setLocation(getLocation().move(nextStep));
                System.out.printf("%s now at %s\n", getName(), getLocation());
                //TODO reschedule this event.

            } else {
                System.out.println("It is finished");
                executor.shutdown();
            }

        };

        scheduledFuture = executor.scheduleAtFixedRate(task, MOVEMENT_SPEED, MOVEMENT_SPEED, TimeUnit.MILLISECONDS);

    }

    public Hex getDestination() {
        if (destination == null) destination = getLocation();
        if (!destination.equals(getLocation())) updateMovement();
        return destination;
    }

    @Override
    public InputStream getRender() {
        try {
            if (getUser() != null) { //user is accessible
                URL url = new URL(getUser().getEffectiveAvatarUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "");
                return connection.getInputStream();
            }
            else return null;
        } catch (MalformedURLException e) {
            System.err.println("Effective avatar URL is malformed!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            Logger.getLogger(PlayerCharacter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Gets this player as though it were a member of the guild it has its allegiance to.
     * @return null if the guild doesn't exist, or if the user isn't in the guild.
     */
    public Member getMember() {
        if (getMap().getGuild() == null) {
            Guild guild = Frostbalance.bot.getGuildFor(getNation());
            if (guild == null) {
                return null;
            } else {
                return guild.getMember(getUser());
            }
        } else {
            return getMap().getGuild().getMember(getUser());
        }
    }

    public String getName() {
        Member member = getMember();
        if (member == null) {
            return getUser().getName();
        } else {
            return member.getEffectiveName();
        }
    }

    public String getTravelTime() {
        return getLocation().minimumDistance(getDestination()) * 2 + " minutes";
    }
}
