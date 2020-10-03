package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.menu.BuildGathererMenu

class BuildGathererCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("gatherer"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return ".gatherer - Shows the build menu, but skips to the gatherer screen"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        BuildGathererMenu(bot, context).send()

    }

}