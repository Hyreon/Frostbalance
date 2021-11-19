package botmanager.doomstack.menu

import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.Menu
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageChannel

class ArgumentObligatorMenu(bot: DoomStack, context: MessageContext) : Menu(bot, context) {

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
