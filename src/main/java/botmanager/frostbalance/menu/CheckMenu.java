package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;

public class CheckMenu extends Menu {

    Guild guild;
    boolean showResult = false;
    HiddenReason hiddenReason = HiddenReason.REFUSED;

    User challenger;

    public final MenuResponse PERFORM_CHECK = new MenuResponse("✅", "Perform check") {

        @Override
        public void reactEvent() {
            showResult = true;
            close(false);
        }

        @Override
        public boolean validConditions() {
            return true;
        }
    };

    public final MenuResponse REFUSE_CHECK = new MenuResponse("❎", "Refuse check") {

        @Override
        public void reactEvent() {
            showResult = false;
            hiddenReason = HiddenReason.REFUSED;
            close(false);
        }

        @Override
        public boolean validConditions() {
            return true;
        }
    };

    public final MenuResponse EXPIRE_CHECK = new MenuResponse(null, "Expire check") {

        @Override
        public void reactEvent() {
            showResult = false;
            hiddenReason = HiddenReason.EXPIRED;
            close(false);
        }

        @Override
        public boolean validConditions() {
            return false;
        }
    };

    public CheckMenu(Frostbalance bot, Guild guild, User challenger) {
        super(bot);
        this.guild = guild;
        this.challenger = challenger;

        menuResponses.add(PERFORM_CHECK);
        menuResponses.add(REFUSE_CHECK);

    }

    @Override
    public EmbedBuilder getMEBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (closed) {
            if (showResult) {
                builder.setColor(bot.getGuildColor(guild));
                builder.setTitle(guild.getMember(challenger).getEffectiveName() + " vs " + guild.getMember(getActor()).getEffectiveName());
                builder.setDescription(check());
            } else {
                builder.setColor(Color.DARK_GRAY);
                builder.setTitle(guild.getMember(challenger).getEffectiveName() + ": Check revoked");
                if (hiddenReason == HiddenReason.REFUSED)
                    builder.setDescription(guild.getMember(getActor()).getEffectiveName() + " refused your check.");
                else if (hiddenReason == HiddenReason.EXPIRED)
                    builder.setDescription("The request expired, as you placed a new one in the same channel.");
            }
        } else {
            builder.setColor(bot.getGuildColor(guild));
            builder.setTitle(guild.getMember(challenger).getEffectiveName() + " has asked " + guild.getMember(getActor()).getEffectiveName() + " to compare influence.");
            builder.setDescription("If they accept, this embed will display who has more influence, but no exact numbers will be shown.");
        }
        return builder;
    }

    private String check() {
        Member firstMember = guild.getMember(challenger);
        Member targetMember = guild.getMember(actor);
        if (bot.getUserInfluence(firstMember).compareTo(bot.getUserInfluence(targetMember)) > 0) {
            return firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".";
        } else if (bot.getUserInfluence(firstMember) == bot.getUserInfluence(targetMember)) {
            if (firstMember.equals(targetMember)) {
                return "To everyone's surprise, " + targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".";
            } else {
                return targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".";
            }
        } else {
            return targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".";
        }
    }

    public User getChallenger() {
        return challenger;
    }

    private enum HiddenReason {
        REFUSED, EXPIRED;
    }
}
