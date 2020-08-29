package botmanager.frostbalance.commands.influence;

import botmanager.frostbalance.Frostbalance;
import botmanager.frostbalance.Influence;
import botmanager.frostbalance.command.AuthorityLevel;
import botmanager.frostbalance.command.MessageContext;
import botmanager.frostbalance.command.ContextLevel;
import botmanager.frostbalance.command.FrostbalanceCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImplicitInfluence extends FrostbalanceCommand {

    SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy hh:mm");
    ArrayList<Member> minuteMembers;
    String cachedDate;

    public ImplicitInfluence(Frostbalance bot) {
        super(bot, new String[] {"implicit"}, AuthorityLevel.GENERIC, ContextLevel.PUBLIC_MESSAGE);
        minuteMembers = new ArrayList<>();
        cachedDate = sdf.format(new Date());
    }

    @Override
    public void run(@NotNull Event genericEvent) {
        GuildMessageReceivedEvent event;
        Guild guild;
        Member member;
        String date = sdf.format(new Date());

        if (!(genericEvent instanceof GuildMessageReceivedEvent)) {
            return;
        }

        event = (GuildMessageReceivedEvent) genericEvent;
        guild = event.getGuild();
        member = event.getMember();

        if (!this.cachedDate.equals(date)) {
            minuteMembers = new ArrayList<>();
            this.cachedDate = date;
        }

        for (Member minuteMember : minuteMembers) {
            if (guild.getId().equals(minuteMember.getGuild().getId()) &&
                    member.getId().equals(minuteMember.getId())) {
                return;
            }
        }

        getBot().getMemberWrapper(event.getAuthor().getId(), event.getGuild().getId()).gainDailyInfluence(new Influence(0.05));
        minuteMembers.add(member);
    }

    @Override
    public void execute(MessageContext eventWrapper, String[] params) {}

    @Override
    public String info(AuthorityLevel authorityLevel, boolean isPublic) {
        if (isPublic) {
            return "type in chat to gain influence gradually (0.05 per minute with a message); this is capped to 1.00 and does not stack with `.daily`.";
        } else {
            return null;
        }
    }
}
