package botmanager.frostbalance.commands.meta

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.input.ConfirmationMenu

class PardonCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("pardon"), AuthorityLevel.NATION_SECURITY, ContextLevel.PUBLIC_MESSAGE) {
    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val args = ArgumentStream(params)
        val memberName = args.exhaust()
        val member = bot.getUserByName(memberName, null)?.memberIfWasIn(context.guild) ?: return context.sendResponse("Could not find player $memberName.")

        if (member.banned) {
            return context.sendResponse("This player's ban is enforced by game staff, and cannot be overridden by national leaders. If you believe this is in error, contact them directly.")
        }

        member.softBanHandler()?.queue({

            ConfirmationMenu(bot, context, {

                member.softPardon()
                return@ConfirmationMenu context.sendResponse("Successfully pardoned ${member.effectiveName}.")

            }, "Are you sure you want to pardon ${member.effectiveName}? You can always undo this later.")
                    .send()

        }, {
            return@queue context.sendResponse("This player is not banned.")
        })

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.pardon** - Pardon a previously banned member"
    }
}