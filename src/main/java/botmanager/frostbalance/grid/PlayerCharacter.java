package botmanager.frostbalance.grid;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Nation;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerCharacter extends TileObject {

    static List<PlayerCharacter> cache = new ArrayList<>();

    public static PlayerCharacter get(String userId, WorldMap map) {
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

    public User getUser() {
        return Frostbalance.bot.getJDA().getUserById(userId);
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

    public void adjustDestination(Hex.Direction direction) {
        if (destination == null) destination = getLocation();
        destination = destination.move(direction);
        updateMovement();
    }

    /**
     * Right now pretty simple: go immediately to the target destination without any waiting or routing.
     */
    private void updateMovement() {
        setLocation(destination);
    }

    @Override
    public InputStream getRender() {
        try {
            URL url = new URL(getUser().getEffectiveAvatarUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            return connection.getInputStream();
        } catch (MalformedURLException e) {
            System.err.println("Effective avatar URL is malformed!");
            e.printStackTrace();
            return null;
        } catch (IOException ex) {
            Logger.getLogger(PlayerCharacter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

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


}
