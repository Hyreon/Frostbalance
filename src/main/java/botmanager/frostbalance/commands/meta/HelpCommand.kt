package botmanager.frostbalance.commands.meta

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.CommandContext
import botmanager.frostbalance.command.FrostbalanceHybridCommandBase

/**
 *
 * @author MC_2018 <mc2018.git></mc2018.git>@gmail.com>
 */
class HelpCommand(bot: Frostbalance) : FrostbalanceHybridCommandBase(bot, arrayOf(
        bot.prefix + "help"
), AuthorityLevel.GENERIC) {
    override fun runHybrid(eventWrapper: CommandContext, vararg params: String) {
        var result = "__**Frostbalance**__\n\n"
        for (command in bot.commands) {
            val info = command.getInfo(eventWrapper)
            if (info != null) {
                result += """
                    $info
                    
                    """.trimIndent()
            }
        }
        eventWrapper.sendResponse(result)
    }

    public override fun info(authorityLevel: AuthorityLevel, isPublic: Boolean): String {
        return if (isPublic) {
            """
     **${bot.prefix}help** in public chat - rattles off a bunch of useless commands
     **${bot.prefix}help** in DM - lists what commands work in PM
     """.trimIndent()
        } else {
            """
     **${bot.prefix}help** in DM - rattles off a bunch of useless commands
     **${bot.prefix}help** in public chat - lists what commands work in public chat
     """.trimIndent()
        }
    }
}