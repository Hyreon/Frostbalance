package botmanager.frostbalance.commands.resource

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.TradeMenu
import botmanager.frostbalance.command.*
import botmanager.frostbalance.commands.influence.SupportCommand
import botmanager.frostbalance.menu.ItemMenu
import botmanager.frostbalance.menu.input.ConfirmationMenu
import botmanager.frostbalance.resource.ItemStack
import java.util.ArrayList

class TradeCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
    "trade",
    "t"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {


    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.__t__rade [PLAYER]** - trade with any player; get near them to complete the trade"
    }

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {
        val arguments = ArgumentStream(params)

        val resultLines: MutableList<String> = ArrayList()
        val bPlayer = context.player!!
        val targetName = arguments.exhaust()
        val targetPlayer = bot.getUserByName(targetName, context.guild)?.playerIfIn(context.gameNetwork)
        if (targetPlayer == null) {
            resultLines.add("Could not find player '$targetName'.")
            context.sendMultiLineResponse(resultLines)
            return
        }
        if (targetPlayer == bPlayer) {
            resultLines.add("You give your entire inventory to yourself. How thoughtful of you.")
            context.sendMultiLineResponse(resultLines)
            return
        }
        TradeMenu(bot, context, targetPlayer.userWrapper).send()

    }


}