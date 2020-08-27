package botmanager.frostbalance.commands.meta

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*

/**
 *
 * @author MC_2018 <mc2018.git></mc2018.git>@gmail.com>
 */
class HelpCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
        "help"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {
    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        var arguments = ArgumentStream(params)

        arguments.next()?.let {
            bot.commands.firstOrNull { command -> command.allAliases.contains(it) }
                    ?.let {
                        context.sendResponse(it.getInfo(context))
                        return
                    }
            return context.sendResponse("Could not find command $it.")
        }

        var result: MutableList<String> = mutableListOf()
        for (command in bot.commands) {
            val info = command.getInfo(context)
            if (info != null) {
                result.add(info)
            }
        }
        context.sendMultiLineResponse(result)
    }

    public override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
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