package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.MessageContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageChannel

class ArgumentObligatorMenu(bot: Frostbalance, context: MessageContext) : Menu(bot, context) {

    var obligationTitle: String? = null
    var obligationDescription: String? = null

    fun getInfluence(parameterName: String, private: Boolean = true): Influence {
        obligationTitle = parameterName
        obligationDescription = "Please type out $parameterName (influence)"
        send(context.privateChannel, context.author)
        return readInfluence(context.privateChannel)
    }

    //FIXME set up a hook for reading this channel's influence
    private fun readInfluence(privateChannel: MessageChannel): Influence {
        return Influence.none()
    }

    override val embedBuilder: EmbedBuilder
        get() = EmbedBuilder()
                .setTitle(obligationTitle)
                .setDescription(obligationDescription)

}
