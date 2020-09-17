package botmanager.frostbalance.commands.meta

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.input.ConfirmationMenu

class BanCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("ban"), AuthorityLevel.NATION_SECURITY, ContextLevel.PUBLIC_MESSAGE) {
    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val args = ArgumentStream(params)
        val memberName = args.exhaust()
        val member = context.guild.getMemberByName(memberName) ?: return context.sendResponse("Could not find player $memberName.");

        ConfirmationMenu(bot, context, {

            if (member.influence > 0) {
                return@ConfirmationMenu context.sendResponse("This player can't be banned because they have influence. Oppose them first.")
            } else if (member.player.isLeader) {
                return@ConfirmationMenu context.sendResponse("This player can't be banned because they are a leader in some nation.")
            }

            member.softBan()
            return@ConfirmationMenu context.sendResponse("Successfully banned ${member.effectiveName}. Begone!")

        }, "Are you sure you want to ban ${member.effectiveName}? You can always undo this later. This will fail if they have any influence.")

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.ban** - Ban a player if their influence is 0"
    }
}