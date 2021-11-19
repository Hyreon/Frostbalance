package botmanager.doomstack.menu

import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.response.MenuResponse
import botmanager.frostbalance.MemberWrapper
import botmanager.frostbalance.menu.CheckMenu
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import kotlin.random.Random

class InvitationMenu(bot: DoomStack, context: MessageContext, var challenger: User) : Menu(bot, context) {

    private var hiddenReason = HiddenReason.REFUSED

    val PERFORM_CHECK: MenuResponse = object : MenuResponse("✅", "Defend your honor!") {
        override fun reactEvent() {
            setDelegating(true)
            var stakes = 120.0
            while (Random.nextBoolean()) {
                stakes *= 2
            }
            redirectTo(ArmySelectMenu(bot, context, challenger, stakes), false)
        }

        override val isValid: Boolean
            get() = true
    }

    val REFUSE_CHECK: MenuResponse = object : MenuResponse("❎", "Surrender instead") {
        override fun reactEvent() {
            hiddenReason = HiddenReason.REFUSED
            close(false)
        }

        override val isValid: Boolean
            get() = true
    }

    val EXPIRE_CHECK: MenuResponse = object : MenuResponse("❓", "Expire check") {
        override fun reactEvent() {
            hiddenReason = HiddenReason.EXPIRED
            close(false)
        }

        override val isValid: Boolean
            get() = false
    }

    init {
        menuResponses.add(PERFORM_CHECK)
        menuResponses.add(REFUSE_CHECK)
    }

    override val embedBuilder: EmbedBuilder
        get() {
            val builder = EmbedBuilder()
            if (isClosed) {
                builder.setColor(Color.DARK_GRAY)
                builder.setTitle(context.guild!!.getMember(challenger)!!.effectiveName + ": Game closed")
                if (hiddenReason == HiddenReason.REFUSED)
                    builder.setDescription(context.guild!!.getMember(actor!!)!!.effectiveName + " surrendered like a true coward.")
                else if (hiddenReason == HiddenReason.EXPIRED)
                    builder.setDescription("The request expired, as you placed a new one in the same channel.")
            } else {
                builder.setTitle(
                    context.guild!!.getMember(challenger)!!.effectiveName + " is attacking " + context.guild!!.getMember(actor!!)!!.effectiveName + "!"
                )
                builder.setDescription("Will you defend your honor?")
            }
            return builder
        }

    private fun check(): String {
        return "CHECK"
    }

    private enum class HiddenReason {
        REFUSED, EXPIRED
    }

}
