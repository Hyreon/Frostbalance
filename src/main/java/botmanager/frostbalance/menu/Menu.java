package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Menu {


    protected Frostbalance bot;

    /**
     * The message this menu is attached to. This can be null if the menu hasn't yet been sent.
     */
    Message message;
    User actor;
    boolean closed = false;

    List<MenuResponse> menuResponses = new ArrayList<>();

    public Menu(Frostbalance bot) {
        this.bot = bot;
    }

    public void send(MessageChannel channel, User actor) {
        bot.addMenu(this);
        this.actor = actor;
        MessageEmbed me = getME();
        if (me.getImage() != null) {
            String fileName = me.getImage().getUrl().replace("attachment://", "");
            message = channel.sendFile(new File(fileName)).embed(me).complete();
        } else {
            message = channel.sendMessage(me).complete();
        }
        updateEmojis();
    }

    public void updateMessage() {
        MessageEmbed me = getME();
        if (me.getImage() != null) {
            String fileName = me.getImage().getUrl().replace("attachment://", "");
            message.delete().queue();
            message = message.getChannel().sendFile(new File(fileName)).embed(me).complete();
        } else {
            message = message.editMessage(getME()).complete();
        }

        updateEmojis();
    }

    private void updateEmojis() {
        try {
            message.clearReactions().queue();
        } catch (IllegalStateException e) {
            /*for (MessageReaction reaction : message.getReactions()) {
                if (reaction.retrieveUsers().complete().contains(bot.getJDA().getSelfUser())) {
                    message.removeReaction(reaction.getReactionEmote().getEmoji()).queue();
                }
            }*/
        }
        if (!closed) {
            for (MenuResponse menuResponse: menuResponses) {
                if (menuResponse.validConditions() || (!message.isFromGuild() && !message.isEdited())) {
                    //TODO don't try to add reactions if the message has been deleted.
                    message.addReaction(menuResponse.emoji).queue();
                }
            }
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public void close(boolean delete) {
        closed = true;
        bot.removeMenu(this);
        if (delete) {
            message.delete().queue();
        } else {
            updateMessage();
        }
    }

    /**
     * Uses the defined MEBuilder and adds the emoji options this menu provides.
     * @return The MessageEmbed this menu is supposed to provide
     */
    public MessageEmbed getME() {
        EmbedBuilder embedBuilder = getMEBuilder();
        if (!closed) {
            String description = "";
            for (MenuResponse menuResponse : menuResponses) {
                if (menuResponse.validConditions()) {
                    description += menuResponse.emoji + " " + menuResponse.name + "\n";
                }
            }
            embedBuilder.addField("Options", description, false);
        }
        return embedBuilder.build();
    }

    public abstract EmbedBuilder getMEBuilder();

    public Message getMessage() {
        return message;
    }

    public void applyResponse(MessageReaction.ReactionEmote reactionEmote) {
        for (MenuResponse menuResponse : menuResponses) {
            if (reactionEmote.getEmoji().equals(menuResponse.emoji)) {
                menuResponse.applyReaction();
            }
        }
    }

    public User getActor() {
        return actor;
    }

    /*
     EmbedBuilder embedBuilder = new EmbedBuilder();
     embedBuilder.setColor(Color.RED);
     embedBuilder.setTitle("Title");
     embedBuilder.setAuthor("Author");
     embedBuilder.setDescription("Description");
     embedBuilder.setFooter("Footer");
     embedBuilder.setImage("https://mobileimages.lowes.com/product/converted/693092/693092000005xl.jpg");
     embedBuilder.setThumbnail("https://images.homedepot-static.com/productImages/21bd11f8-81e9-4ea6-a9c8-cba1ed8119e7/svn/bricks-red0126mco-64_300.jpg");
     embedBuilder.addField("", "Value1", false);
     return embedBuilder.build();
     */

}
