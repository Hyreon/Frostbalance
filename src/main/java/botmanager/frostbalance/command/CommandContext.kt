package botmanager.frostbalance.command

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.GameNetwork
import botmanager.frostbalance.GuildWrapper
import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.menu.option.ListMenu
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import java.util.*

open class CommandContext {
    @JvmField
    var bot: Frostbalance
    var privateEvent: PrivateMessageReceivedEvent? = null
    @JvmField
    var publicEvent: GuildMessageReceivedEvent? = null

    constructor(bot: Frostbalance, privateEvent: PrivateMessageReceivedEvent?) {
        this.bot = bot
        this.privateEvent = privateEvent
    }

    constructor(bot: Frostbalance, publicEvent: GuildMessageReceivedEvent?) {
        this.bot = bot
        this.publicEvent = publicEvent
    }

    constructor(bot: Frostbalance, genericEvent: Event?) {
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

    @get:Deprecated("")
    val jdaUser: User
        get() = if (isPublic) {
            publicEvent!!.author
        } else privateEvent!!.author

    val author: UserWrapper
        get() = bot.getUserWrapper(jdaUser.id)

    val message: Message
        get() = if (isPublic) {
            publicEvent!!.message
        } else privateEvent!!.message

    val channel: MessageChannel
        get() = if (isPublic) {
            publicEvent!!.channel
        } else privateEvent!!.channel

    val privateChannel: MessageChannel
        get() = Objects.requireNonNull(author.jdaUser)!!.openPrivateChannel().complete()

    fun sendResponse(message: String?) {
        val messageEmbed = buildEmbed(message)
        if (isPublic) {
            Utilities.sendGuildMessage(channel as TextChannel, messageEmbed)
        } else {
            Utilities.sendPrivateMessage(jdaUser, messageEmbed)
        }
    }

    fun sendEmbedResponse(resultLines: List<String?>) {
        if (resultLines.size > 10) {
            object : ListMenu<String?>(bot, this, resultLines) {}.send(channel, author)
        } else {
            val message = java.lang.String.join("\n", resultLines)
            val messageEmbed = buildEmbed(message)
            if (isPublic) {
                Utilities.sendGuildMessage(channel as TextChannel, messageEmbed)
            } else {
                Utilities.sendPrivateMessage(jdaUser, messageEmbed)
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
    fun buildEmbed(message: String?): MessageEmbed {
        return EmbedBuilder()
                .setDescription(message)
                .setColor(guild?.color)
                .setFooter(if (isPublic) null else guild?.contextFooter())
                .build()
    }

    fun sendPrivateResponse(message: String?) {
        val messageEmbed = buildEmbed(message)
        Utilities.sendPrivateMessage(jdaUser, messageEmbed)
    }

    open val authority: AuthorityLevel
        get() = if (hasGuild()) {
            GuildCommandContext(this).authority
        } else author.authority
    val event: Event?
        get() = if (isPublic) publicEvent else privateEvent

    fun hasGuild(): Boolean {
        return if (isPublic) {
            true
        } else {
            println(author.defaultGuildId)
            author.defaultGuildId != null
        }
    }

    open val guild: GuildWrapper?
        get() = if (!hasGuild()) null else {
            GuildCommandContext(this).guild
        }
    open val gameNetwork: GameNetwork
        get() = bot.getGameNetwork(author.defaultNetworkId)
}