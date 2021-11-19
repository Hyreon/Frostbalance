package botmanager.doomstack

import botmanager.frostbalance.command.AuthorityLevel
import botmanager.generic.ICommand
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import java.util.*

abstract class DoomStackCommand(protected var bot: DoomStack, protected val aliases: Array<String>) : ICommand {

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

    protected abstract fun info(): String?

    val mainAlias: String
        get() {
            val mainAlias = aliases[0]
            return if (aliases.contains(mainAlias[0].toString())) {
                "__${mainAlias[0]}__${mainAlias.substring(1)}"
            } else {
                mainAlias
            }
        }

    val alternativeAliases: List<String>
        get() = aliases.asList().subList(1, aliases.size)

    val allAliases: List<String>
        get() = aliases.toList()

    companion object {
        const val SPEED_TESTS = true
    }
}