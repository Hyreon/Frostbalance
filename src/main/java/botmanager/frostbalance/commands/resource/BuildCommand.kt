package botmanager.frostbalance.commands.resource

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.ContextLevel
import botmanager.frostbalance.command.FrostbalanceGuildCommand
import botmanager.frostbalance.command.GuildMessageContext
import botmanager.frostbalance.menu.BuildGathererMenu
import botmanager.frostbalance.menu.BuildMenu

class BuildCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("build"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.build** - Shows the building menu"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        BuildMenu(bot, context).send()

    }

}