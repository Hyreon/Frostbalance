package botmanager.doomstack

import botmanager.doomstack.menu.Menu
import botmanager.frostbalance.GuildWrapper
import botmanager.frostbalance.UserWrapper
import botmanager.generic.BotBase
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import java.util.*

class DoomStack(botToken: String?, name: String?) : BotBase(botToken, name) {

    val prefix: String
        get() = "."

    companion object {
        lateinit var bot: DoomStack
    }

    private val activeMenus: MutableList<Menu> = ArrayList()

    //TODO run commands asynchronously so that it can wait for user input on some commands
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        for (command in commands) {
            command.run(event)
        }
        for (menu in activeMenus.filter { menu -> menu.hasHook }) {
            menu.hook!!.readMessage(MessageContext(this, event))
        }
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        for (command in commands) {
            command.run(event)
        }
        for (menu in activeMenus.filter { menu -> menu.hasHook }) {
            menu.hook!!.readMessage(MessageContext(this, event))
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        getActiveMenus().firstOrNull { menu ->
            event.userId == menu.actor?.id && menu.message?.id == event.messageId
        }?.applyResponse(event.reactionEmote)
    }

    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) {
        getActiveMenus().firstOrNull { menu ->
            event.userId == menu.actor?.id && menu.message?.id == event.messageId
        }?.applyResponse(event.reactionEmote)
    }

    private fun getActiveMenus(): List<Menu> {
        return activeMenus
    }

    fun addMenu(menu: Menu) {
        activeMenus.add(menu)
    }

    fun removeMenu(menu: Menu) {
        activeMenus.remove(menu)
    }

    override fun getCommands(): Array<DoomStackCommand> {
        val commands = super.getCommands()
        val newCommands = arrayOfNulls<DoomStackCommand>(commands.size)
        for (i in commands.indices) {
            newCommands[i] = commands[i] as DoomStackCommand
        }
        return newCommands.requireNoNulls()
    }

    init {
        bot = this
        jda.presence.activity = Activity.of(Activity.ActivityType.DEFAULT, prefix + "attack!")
        commands = arrayOf(
            AttackCommand(this)
        )
    }

    fun getUserByName(targetName: String, guild: Guild?): User? {
        return guild?.let { guild.members.firstOrNull { member -> member?.effectiveName == targetName }?.user}
            ?: jda.users.firstOrNull { user -> println(user.name); user.name == targetName }
    }


}