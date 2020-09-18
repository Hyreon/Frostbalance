package botmanager.frostbalance.commands.meta

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceCommand
import botmanager.frostbalance.command.MessageContext

class TriangleCommand(bot: Frostbalance) : FrostbalanceCommand(bot, arrayOf("levelwith"), AuthorityLevel.GENERIC, ContextLevel.ANY) {
    override fun execute(context: MessageContext, params: Array<String>) {
        return context.sendResponse(params?.getOrNull(0)?.let {
            "Triangle of $it: ${Utilities.triangulateWithRemainder(it.toDouble())}"
        })
    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "Find the cost of a tile level with .levelwith NUMBER"
    }

}
