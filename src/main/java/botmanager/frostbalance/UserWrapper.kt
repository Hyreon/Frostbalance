package botmanager.frostbalance

import botmanager.Utilities
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.Container
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException
import java.awt.image.BufferedImage
import java.util.*

/**
 * A special user class that contains data about a player that is unique to Frostbalance.
 * Extension is not an option, as User is actually an interface. Additionally, BotUsers are not
 * guaranteed to share any server with the main bot; this is intentional, so that players who
 * cease to play (or are banned) do not cease the game's functionality.
 */
class UserWrapper(bot: Frostbalance, userId: String) : Container, Containable<Frostbalance> {

    val authority: AuthorityLevel
     get() = minimumAuthorityLevel

    @Transient
    var bot: Frostbalance = bot

    var id: String = userId

    /**
     * A list of a member instances of this player.
     */
    var memberReference: MutableSet<MemberWrapper> = HashSet()

    /**
     * A list of independent players this user controls.
     * There is only one player per GameInstance, but there
     * may be no player in a GameInstance if that player
     * hasn't ever joined the server.
     */
    private var playerReference: MutableSet<Player> = HashSet()

    @Transient
    var userIcon: BufferedImage? = null

    private var lastKnownName: String? = null

    var defaultGuildId: String? = null
    var defaultNetworkId: String = "main"
    var minimumAuthorityLevel: AuthorityLevel = AuthorityLevel.GENERIC
    var globallyBanned = false

    init {
        if (jdaUser?.mutualGuilds?.size == 1) { //set default guild as the first one they join
            defaultGuildId = jdaUser!!.mutualGuilds[0].id
            defaultNetworkId = defaultGuild!!.gameNetwork.id
        }
    }

    /**
     * Gets this user as a member of the guild with the specified id.
     * @param id The id of the guild this user is supposedly a member of.
     * @return The MemberWrapper referencing this UserWrapper and a GuildId
     */
    fun memberIn(id: String): MemberWrapper {

        var botMember = memberReference.find { member: MemberWrapper -> member.guildId == id }
        if (botMember == null) {
            botMember = MemberWrapper(this, id)
            memberReference.add(botMember)
        }
        return botMember
    }

    fun memberIn(guild: GuildWrapper): MemberWrapper {

        var botMember = memberReference.find { member: MemberWrapper -> member.guildId == guild.id }
        if (botMember == null) {
            botMember = MemberWrapper(this, guild.id)
            memberReference.add(botMember)
        }
        return botMember

    }

    /**
     * Gets an instance of a MemberWrapper *only if* this user is present in this guild,
     * and if the bot has access to this guild. Otherwise it will return null.
     * This will also return null if the member WAS in the guild, but no longer is.
     */
    fun memberIfIn(guild: GuildWrapper): MemberWrapper? {
        return guild.jdaGuild?.getMemberById(id)?.wrapper
    }

    val jdaUser: User?
        get() = jda.getUserById(id)
    val defaultGuild: GuildWrapper?
        get() = defaultGuildId?.let { bot.getGuildWrapper(it) }
    val jda: JDA
        get() = bot.jda
    val name: String
        get() = jdaUser?.name ?: "*Deleted User*"

    fun resetDefaultGuild() {
        defaultGuildId = null
    }

    fun loadLegacy(UserDefaultGuild: String, GloballyBanned: Boolean, Name: String, isBotAdmin: Boolean) {

        defaultGuildId = UserDefaultGuild
        globallyBanned = GloballyBanned
        lastKnownName = Name
        minimumAuthorityLevel = if (isBotAdmin) AuthorityLevel.BOT_ADMIN else AuthorityLevel.GENERIC
    }

    fun globalBan() {
        globallyBanned = true
        for (member in memberReference) {
            try {
                jdaUser?.let{ member.member?.guild?.ban(it, 0)?.queue() }
            } catch (e: HierarchyException) {
                System.err.println("Unable to fully ban user " + member.effectiveName + " because they have admin privileges in some servers!")
                e.printStackTrace()
            }
        }
    }

    fun globalPardon(): Boolean {
        return if (globallyBanned) {
            globallyBanned = false
            for (member in memberReference) {
                try {
                    jdaUser?.let {member.guildWrapper.jdaGuild?.unban(it)?.queue()}
                } catch (e: ErrorResponseException) {
                    //nothing
                }
            }
            true
        } else false
    }

    constructor(bot: Frostbalance, user: User) : this(bot, user.id) {
        Objects.requireNonNull(bot)
        Objects.requireNonNull(user)
        this.bot = bot
        id = user.id
        lastKnownName = user.name
    }

    override fun adopt() {
        if (memberReference == null) {
            memberReference = HashSet()
        }
        for (member in memberReference) {
            member.setParent(this)
        }
        if (playerReference == null) {
            playerReference = HashSet()
        }
        for (player in playerReference) {
            player.setParent(this)
        }
    }

    override fun setParent(parent: Frostbalance) {
        bot = parent
    }

    /**
     * Return this user as if it were a player in the given game instance.
     * If this player hasn't yet joined this game, then it creates a player (but not necessarily a character).
     */
    fun playerIn(gameNetwork: GameNetwork): Player {
        return playerReference.firstOrNull { player -> player.gameNetwork == gameNetwork} ?: let {
            val player = Player(gameNetwork.id, this)
            playerReference.add(player)
            return player
        }
    }

    fun sendNotification(guild: GuildWrapper? = null, content: String) {
        println("Sending notification")
        Utilities.sendPrivateMessage(jdaUser,
                EmbedBuilder()
                        .setDescription(content)
                        .setColor(guild?.color)
                        .setFooter(guild?.grantedContextFooter()).build())
    }
}

val User.wrapper: UserWrapper
    get() = Frostbalance.bot.getUserWrapper(id)