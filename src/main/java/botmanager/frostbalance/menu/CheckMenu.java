package botmanager.frostbalance.menu;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.MemberWrapper;
import botmanager.frostbalance.UserWrapper;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.menu.response.MenuResponse;
import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class CheckMenu extends Menu {

    boolean showResult = false;
    HiddenReason hiddenReason = HiddenReason.REFUSED;

    UserWrapper challenger;

    public final MenuResponse PERFORM_CHECK = new MenuResponse("✅", "Perform check") {

        @Override
        public void reactEvent() {
            showResult = true;
            close(false);
        }

        @Override
        public boolean isValid() {
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
        public boolean isValid() {
            return true;
        }
    };

    public final MenuResponse EXPIRE_CHECK = new MenuResponse("❓", "Expire check") {

        @Override
        public void reactEvent() {
            showResult = false;
            hiddenReason = HiddenReason.EXPIRED;
            close(false);
        }

        @Override
        public boolean isValid() {
            return false;
        }

    };

    public CheckMenu(Frostbalance bot, MessageContext context, UserWrapper challenger) {
        super(bot, context);
        this.challenger = challenger;

        menuResponses.add(PERFORM_CHECK);
        menuResponses.add(REFUSE_CHECK);

    }

    @Override
    public EmbedBuilder getEmbedBuilder() {
        EmbedBuilder builder = new EmbedBuilder();
        if (isClosed()) {
            if (showResult) {
                builder.setColor(getBot().getGuildWrapper(getContext().getGuild().getId()).getColor());
                builder.setTitle(getContext().getGuild().getMember(challenger).getEffectiveName() + " vs " + getActor().memberIn(getContext().getGuild().getId()).getEffectiveName());
                builder.setDescription(check());
            } else {
                builder.setColor(Color.DARK_GRAY);
                builder.setTitle(getContext().getGuild().getMember(challenger).getEffectiveName() + ": Check revoked");
                if (hiddenReason == HiddenReason.REFUSED)
                    builder.setDescription(getActor().memberIn(getContext().getGuild().getId()).getEffectiveName() + " refused your check.");
                else if (hiddenReason == HiddenReason.EXPIRED)
                    builder.setDescription("The request expired, as you placed a new one in the same channel.");
            }
        } else {
            builder.setColor(getBot().getGuildWrapper(getContext().getGuild().getId()).getColor());
            builder.setTitle(getContext().getGuild().getMember(challenger).getEffectiveName() + " has asked " + getActor().memberIn(getContext().getGuild()).getEffectiveName() + " to compare influence.");
            builder.setDescription("If they accept, this embed will display who has more influence, but no exact numbers will be shown.");
        }
        return builder;
    }

    private String check() {
        MemberWrapper firstMember = getBot().getMemberWrapper(challenger.getId(), getContext().getGuild().getId());
        MemberWrapper targetMember = getActor().memberIn(getContext().getGuild());
        if (firstMember.getInfluence().compareTo(targetMember.getInfluence()) > 0) {
            return firstMember.getEffectiveName() + " has **more** influence than " + targetMember.getEffectiveName() + ".";
        } else if (firstMember.getInfluence().equals(targetMember.getInfluence())) {
            if (firstMember.equals(targetMember)) {
                return "To everyone's surprise, " + targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".";
            } else {
                return targetMember.getEffectiveName() + " has *as much* influence as " + firstMember.getEffectiveName() + ".";
            }
        } else {
            return targetMember.getEffectiveName() + " has **more** influence than " + firstMember.getEffectiveName() + ".";
        }
    }

    public UserWrapper getChallenger() {
        return challenger;
    }

    private enum HiddenReason {
        REFUSED, EXPIRED;
    }
}
