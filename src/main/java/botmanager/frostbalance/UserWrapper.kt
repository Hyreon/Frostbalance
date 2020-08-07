package botmanager.frostbalance

import botmanager.frostbalance.command.AuthorityLevel
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import java.awt.image.BufferedImage
import java.util.*

/**
 * A special user class that contains data about a player that is unique to Frostbalance.
 * Extension is not an option, as User is actually an interface. Additionally, BotUsers are not
 * guaranteed to share any server with the main bot; this is intentional, so that players who
 * cease to play (or are banned) do not cease the game's functionality.
 */
class UserWrapper(bot: Frostbalance, userId: String) {

    @Transient
    var bot: Frostbalance = bot

    var userId: String = userId

    /**
     * A list of a member instances of this player.
     */
    var memberReference: MutableList<MemberWrapper> = ArrayList()

    @Transient
    var userIcon: BufferedImage? = null

    private var lastKnownName: String? = null

    var allegiance = Nation.NONE

    var defaultGuildId: String? = null
    var minimumAuthorityLevel: AuthorityLevel = AuthorityLevel.GENERIC
    var globallyBanned = false

    /**
     * Gets this user as a member of the guild with the specified id.
     * @param id The id of the guild this user is supposedly a member of.
     * @return The MemberWrapper referencing this UserWrapper and a GuildId
     */
    fun getMember(id: String): MemberWrapper {

        var botMember = memberReference.find { member: MemberWrapper -> member.guildId == id }
        if (botMember == null) {
            botMember = MemberWrapper(this, id)
            memberReference.add(botMember)
        }
        return botMember
    }

    fun getMember(guild: GuildWrapper): MemberWrapper {

        var botMember = memberReference.find { member: MemberWrapper -> member.guildId == guild.id }
        if (botMember == null) {
            botMember = MemberWrapper(this, guild.id)
            memberReference.add(botMember)
        }
        return botMember

    }

    val user: User? //may be null if the user is now inaccessible
        get() = bot.jda.getUserById(userId)
    val defaultBotGuild: GuildWrapper?
        get() = bot.getGuildWrapper(defaultGuildId)
    val jda: JDA
        get() = bot.jda
    val name: String
        get() = user?.name ?: "Deleted User"

    fun resetDefaultGuild() {
        defaultGuildId = null
    }

    fun legacyLoad(MainAllegiance: Nation, UserDefaultGuild: String, GloballyBanned: Boolean, Name: String, isBotAdmin: Boolean) {

        allegiance = MainAllegiance
        defaultGuildId = UserDefaultGuild
        globallyBanned = GloballyBanned
        lastKnownName = Name
        minimumAuthorityLevel = if (isBotAdmin) AuthorityLevel.BOT_ADMIN else AuthorityLevel.GENERIC
    }

    fun load(frostbalance: Frostbalance) {
        bot = frostbalance
        for (memberWrapper in memberReference) {
            memberWrapper.userWrapper = this
        }
    }

    constructor(bot: Frostbalance, user: User) : this(bot, user.id) {
        Objects.requireNonNull(bot)
        Objects.requireNonNull(user)
        this.bot = bot
        userId = user.id
        lastKnownName = user.name
    }

    val User.wrapper: UserWrapper
        get() = bot.getUserWrapper(id)
}