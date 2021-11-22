package botmanager.doomstack

import botmanager.doomstack.menu.InvitationMenu
import botmanager.frostbalance.MapToCollection
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.internal.utils.tuple.Pair
import kotlin.concurrent.schedule
import java.util.*

class AttackCommand(bot: DoomStack): DoomStackCommand(bot, arrayOf(
    "attack",
    "doomstack",
    "ds"
)) {

    override fun execute(context: MessageContext, params: Array<String>) {
        runPublic(context, params.joinToString(" "))
    }

    var privateCheckRequests = MapToCollection<TextChannel, MutableCollection<Pair<User, User>>>()
    var checkMenuCache = MapToCollection<TextChannel, MutableCollection<InvitationMenu>>()

    fun runPublic(context: MessageContext, message: String) {
        val result: String?
        val targetUser: User?
        if (message.isEmpty()) {
            result = info()
            context.sendResponse(result)
            return
        }
        targetUser = bot.getUserByName(message, context.guild)
        if (targetUser == null) {
            result = "Couldn't find user '$message'."
            context.sendResponse(result)
            return
        }
        val menu = InvitationMenu(bot, context, context.author)
        menu.send(context.channel, targetUser)
        addToCheckCache(context.channel as TextChannel, menu)
        if (targetUser.jda.selfUser == targetUser) {
            Timer("SettingUp", false).schedule(500) {
                menu.PERFORM_CHECK.applyReaction()
            }
        }
    }

    /**
     * Adds an item to the checkMenu cache. If multiple menus with the same pair of players exist on a channel,
     * the older prompt is disabled.
     * @param channel
     * @param menuToAdd
     */
    private fun addToCheckCache(channel: TextChannel, menuToAdd: InvitationMenu) {
        val channelCheckRequests: MutableCollection<InvitationMenu> = checkMenuCache.getOrDefault(channel, ArrayList())
        var menuToRemove: InvitationMenu? = null
        for (menu in channelCheckRequests) {
            if (menu.actor == menuToAdd.actor && menu.challenger == menuToAdd.challenger) {
                menuToRemove = menu
                break
            }
        }
        if (menuToRemove != null) {
            if (!menuToRemove.isClosed) {
                menuToRemove.EXPIRE_CHECK.reactEvent()
            }
            channelCheckRequests.remove(menuToRemove)
        }
        channelCheckRequests.add(menuToAdd)
    }

    protected override fun info(): String {
        return "**" + bot.prefix + "attack** PLAYER - play doomstack with another player (no pings)"
    }

}