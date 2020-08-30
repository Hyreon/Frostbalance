package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceCommand
import botmanager.frostbalance.command.MessageContext
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.text.SimpleDateFormat
import java.util.*

class ImplicitSubscription(bot: Frostbalance?) : FrostbalanceCommand(bot!!, arrayOf("implicit"), AuthorityLevel.GENERIC, ContextLevel.PUBLIC_MESSAGE) {
    var sdf = SimpleDateFormat("M/dd/yyyy hh:mm")
    var minuteMembers: ArrayList<Member?>
    var cachedDate: String
    override fun run(genericEvent: Event) {
        val guild: Guild
        val member: Member?
        val date = sdf.format(Date())
        if (genericEvent !is GuildMessageReceivedEvent) {
            return
        }
        val event: GuildMessageReceivedEvent = genericEvent
        bot.getMemberWrapper(event.author.id, event.guild.id).updateSubscription()
    }

    public override fun execute(eventWrapper: MessageContext?, params: Array<String>?) {}
    public override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return if (isPublic) {
            "type in chat to gain influence gradually (0.05 per minute with a message); this is capped to 1.00 and does not stack with `.daily`."
        } else {
            null
        }
    }

    init {
        minuteMembers = ArrayList()
        cachedDate = sdf.format(Date())
    }
}