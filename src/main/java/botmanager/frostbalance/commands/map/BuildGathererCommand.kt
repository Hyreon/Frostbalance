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

        if (context.author.playerIn(context.gameNetwork).character.tile.resourceData.priorityOrderDeposits().isEmpty()) {
            return context.sendResponse("There are no resources to gather on this tile! You'll need to find some with `.search`.\n" +
                    "This will take 10 minutes on average, but maybe longer.")
        }
        BuildGathererMenu(bot, context).send()

    }

}