package botmanager.frostbalance.commands.player

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext

class QueueCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("queue"), AuthorityLevel.GENERIC, ContextLevel.ANY) {
    //override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
    //    return ".queue <MOVE/CLAIM/TRANSFER> ... - queues an action after you have reached your current destination."
    //}

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.queue** - Shows a queue of all your planned actions."
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String?) {
        context.sendMultiLineResponse(context.player.character.actionQueue.map { it.displayStep() })
    }

}