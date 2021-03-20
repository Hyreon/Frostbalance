package botmanager.frostbalance.commands.influence

import botmanager.frostbalance.Frostbalance
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
    var minuteMembers: ArrayList<Member?> = ArrayList()
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

    override fun execute(context: MessageContext, params: Array<String>) {}

    public override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return if (isPublic) {
            "type in chat to update your subscriptions."
        } else {
            null
        }
    }

    init {
        cachedDate = sdf.format(Date())
    }
}