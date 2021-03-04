package botmanager.frostbalance.commands.resource

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.commands.influence.SupportCommand
import botmanager.frostbalance.menu.ItemMenu
import botmanager.frostbalance.menu.input.ConfirmationMenu
import botmanager.frostbalance.resource.ItemStack
import java.util.ArrayList

class GiveItemCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf(
    "give",
    "g"
), AuthorityLevel.GENERIC, ContextLevel.ANY) {


    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String {
        return "**.__g__ive [PLAYER]** - give a nearby player some items"
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
        //TODO
        /*
        val transferStack = ItemMenu().getItem {
            resultLines.add(bPlayer.effectiveName + " has *supported* " + targetMember.effectiveName + ", increasing their influence here.")
            Utilities.sendPrivateMessage(targetMember.userWrapper.jdaUser, context.buildEmbed(String.format("%s has *supported* you, increasing your influence in %s by %s.",
                bPlayer.effectiveName,
                context.guild.name,
                transferAmount), false))
            targetMember.adjustInfluence(transferAmount)
            context.sendMultiLineResponse(resultLines)
            return
        }*/

    }


}