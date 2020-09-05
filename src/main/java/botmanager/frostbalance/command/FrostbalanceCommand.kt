package botmanager.frostbalance.command

import botmanager.frostbalance.Frostbalance
import botmanager.generic.ICommand
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import java.util.*

abstract class FrostbalanceCommand(protected var bot: Frostbalance, protected val aliases: Array<String>, val requiredAuthority: AuthorityLevel, private val requiredContext: ContextLevel = ContextLevel.ANY) : ICommand {

    /**
     * Standard command strucuture. Execute can imply any number of things.
     * @param genericEvent
     */
    override fun run(genericEvent: Event) {
        var parameters: Array<String>
        val context: MessageContext
        context = try {
            MessageContext(bot, genericEvent)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return
        }
        if (!hasAlias(genericEvent)) return
        parameters = minifyMessage(context.message.contentRaw)!!.split(" ".toRegex()).toTypedArray()
        if (parameters.size == 1 && parameters[0].isEmpty()) {
            parameters = arrayOf()
        }
        if (!wouldAuthorize(context.authority)) {
            context.sendResponse("You don't have sufficient privileges to do this.")
            return
        }
        if (requiredContext == ContextLevel.PRIVATE_MESSAGE && context.isPublic) {
            context.sendResponse("This command can only be run via DM.")
            return
        }
        if (requiredContext == ContextLevel.PUBLIC_MESSAGE && !context.isPublic) {
            context.sendResponse("This command can only be run from within a channel on the guild.")
            return
        }
        if (SPEED_TESTS) {
            val startTime = System.nanoTime()
            try {
                execute(context, parameters)
            } catch (e: Exception) {
                e.printStackTrace()
                context.sendMultiLineResponse(ArrayList(setOf("An internal error occurred when performing this command.")))
            }
            val stopTime = System.nanoTime()
            val elapsedTime = stopTime - startTime
            println((elapsedTime * 1e-9).toString() + " seconds to execute " + javaClass.simpleName)
        } else {
            execute(context, parameters)
        }
    }


    protected abstract fun execute(context: MessageContext, params: Array<String>)

    fun hasAlias(genericEvent: Event?): Boolean {
        val message: String = when (genericEvent) {
            is GuildMessageReceivedEvent -> {
                genericEvent.message.contentRaw
            }
            is PrivateMessageReceivedEvent -> {
                genericEvent.message.contentRaw
            }
            else -> {
                return false
            }
        }
        for (alias in aliases) {
            val effectiveAlias = bot.prefix + alias
            if (message.equals(effectiveAlias, ignoreCase = true)) {
                return true
            } else if (message.startsWith("$effectiveAlias ")) {
                return true
            }
        }
        return false
    }

    fun minifyMessage(message: String): String? {
        for (keyword in aliases) {
            val effectiveAlias = bot.prefix + keyword
            if (message.equals(effectiveAlias, ignoreCase = true)) {
                return message.replace(effectiveAlias, "")
            } else if (message.startsWith("$effectiveAlias ")) {
                return message.replace("$effectiveAlias ", "")
            }
        }
        return null
    }

    /**
     * Does this user, with this guild, have the authority to run this command at its lowest authority level?
     * @return Whether the user could run this command
     */
    fun wouldAuthorize(authorityLevel: AuthorityLevel): Boolean {
        return authorityLevel.hasAuthority(requiredAuthority)
    }

    /**
     * Gets the public info of a thing. This is predefined to actually use the internal info command
     * and wrap around it, not showing anything the user doesn't have authority to see.
     * @param context the context of the info gotten
     * @return
     */
    open fun getInfo(context: MessageContext): String? {
        return if (context.authority.hasAuthority(requiredAuthority)) {
            info(context.authority, context.isPublic)
        } else null
    }

    protected abstract fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String?

    val mainAlias: String
        get() = {
            val mainAlias = aliases[0]
            if (aliases.contains(mainAlias[0].toString())) {
                "__${mainAlias[0]}__${mainAlias.substring(1)}"
            } else {
                mainAlias
            }
        }.invoke()

    val alternativeAliases: List<String>
        get() = aliases.asList().subList(1, aliases.size)

    val allAliases: List<String>
        get() = aliases.toList()

    companion object {
        const val SPEED_TESTS = true
    }
}