package botmanager.doomstack

import botmanager.Utilities
import botmanager.doomstack.menu.option.ListMenu
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import java.awt.Color
import java.util.*

open class MessageContext {
    @JvmField
    var bot: DoomStack
    var privateEvent: PrivateMessageReceivedEvent? = null
    @JvmField
    var publicEvent: GuildMessageReceivedEvent? = null

    constructor(bot: DoomStack, privateEvent: PrivateMessageReceivedEvent?) {
        this.bot = bot
        this.privateEvent = privateEvent
    }

    constructor(bot: DoomStack, publicEvent: GuildMessageReceivedEvent?) {
        this.bot = bot
        this.publicEvent = publicEvent
    }

    constructor(bot: DoomStack, genericEvent: Event?) {
        if (genericEvent is PrivateMessageReceivedEvent) {
            this.bot = bot
            privateEvent = genericEvent
        } else if (genericEvent is GuildMessageReceivedEvent) {
            this.bot = bot
            publicEvent = genericEvent
        } else throw IllegalStateException("CommandContext cannot be initialized with this sort of event!")
    }

    val isPublic: Boolean
        get() {
            if (publicEvent != null) return true
            if (privateEvent != null) return false
            throw IllegalStateException("CommandContext is neither public nor private, as no valid event was found!")
        }
    val jDA: JDA
        get() = if (isPublic) {
            publicEvent!!.jda
        } else privateEvent!!.jda

    val author: User
        get() = if (isPublic) {
            publicEvent!!.author
        } else privateEvent!!.author

    val guild: Guild?
        get() = if (isPublic) {
            publicEvent!!.guild
        } else null

    val message: Message
        get() = if (isPublic) {
            publicEvent!!.message
        } else privateEvent!!.message

    val channel: MessageChannel
        get() = if (isPublic) {
            publicEvent!!.channel
        } else privateEvent!!.channel

    val privateChannel: MessageChannel
        get() = Objects.requireNonNull(author)!!.openPrivateChannel().complete()

    fun sendResponse(message: String?) {
        val messageEmbed = buildEmbed(message)
        if (isPublic) {
            Utilities.sendGuildMessage(channel as TextChannel, messageEmbed)
        } else {
            Utilities.sendPrivateMessage(author, messageEmbed)
        }
    }

    @JvmOverloads
    fun sendMultiLineResponse(resultLines: List<String?>) {
        if (resultLines.size > 10) {
            object : ListMenu<String?>(bot, this, resultLines) {

                override val embedBuilder: EmbedBuilder
                    get() = super.embedBuilder
                        .setTitle("Page $page/${maxPages()}")

            }.send(channel, author)
        } else {
            val message = java.lang.String.join("\n", resultLines)
            val messageEmbed = buildEmbed(message)
            if (isPublic) {
                Utilities.sendGuildMessage(channel as TextChannel, messageEmbed)
            } else {
                Utilities.sendPrivateMessage(author, messageEmbed)
            }
        }
    }

    /**
     * Generates an embed with the given message.
     * This embed will use the color associated with the guild this occurs in,
     * and display in the footer the guild this occurs in (if done in a private window.)
     * @param message
     * @return
     */
    fun buildEmbed(message: String?, internal: Boolean = true): MessageEmbed {
        return EmbedBuilder()
            .setDescription(message)
            .setColor(Color.RED)
            .build()
    }

    fun sendPrivateResponse(message: String?) {
        val messageEmbed = buildEmbed(message)
        Utilities.sendPrivateMessage(author, messageEmbed)
    }


    val event: Event?
        get() = if (isPublic) publicEvent else privateEvent
}