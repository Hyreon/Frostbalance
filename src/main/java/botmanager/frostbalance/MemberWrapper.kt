package botmanager.frostbalance

import botmanager.frostbalance.GuildWrapper.Companion.wrapper
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.grid.Containable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member

class MemberWrapper(@Transient var userWrapper: UserWrapper, var guildId: String) : Containable<UserWrapper?> {


    val authority: AuthorityLevel
        get() = bot.getAuthority(guildWrapper.guild!!, userWrapper.user!!)
    private var lastKnownNickname: String?

    var influence: Influence = Influence(0)
    var dailyInfluence = DailyInfluenceSource()
    var locallyBanned = false
    val banned: Boolean
        get() = userWrapper.globallyBanned || locallyBanned
    private val userId: String
        get() = userWrapper.userId

    fun adjustInfluence(amount: Influence?) {
        influence = influence.add(amount)
        if (influence.isNegative) {
            influence = Influence.none()
        }
    }

    fun gainDailyInfluence(): Influence {
        return gainDailyInfluence(DailyInfluenceSource.DAILY_INFLUENCE_CAP)
    }

    fun gainDailyInfluence(influenceRequested: Influence): Influence {
        val influenceGained = dailyInfluence.yield(influenceRequested)
        influence = influence.add(influenceGained)
        return influenceGained
    }

    val effectiveName: String?
        get() {
            val nickname = member?.nickname
            if (nickname != null) {
                lastKnownNickname = nickname
            }
            return lastKnownNickname?:userWrapper.name
        }

    /**
     *
     * @return The member if extant, or null if the bot has been removed from the relevant guild,
     * or the relevant player has left from it.
     */
    val member: Member?
        get() = jda.getGuildById(guildId)?.getMemberById(userId)
    val online: Boolean
        get() = member != null

    private val guildWrapper: GuildWrapper
        get() = userWrapper.bot.getGuildWrapper(guildId)
    private val guild: Guild?
        get() = jda.getGuildById(guildId)
    private val bot: Frostbalance
        get() = userWrapper.bot
    private val jda: JDA
        get() = userWrapper.jda
    val influenceSource: DailyInfluenceSource
        get() {
            if (!dailyInfluence.isActive) {
                dailyInfluence = DailyInfluenceSource()
            }
            return dailyInfluence
        }

    fun hasBeenForciblyRemoved(): Boolean {
        return guildWrapper.hasBeenForciblyRemoved(userId)
    }

    fun loadLegacy(LocallyBanned: Boolean, dailyInfluenceSource: DailyInfluenceSource, UserInfluence: Influence, Nickname: String?, User: UserWrapper) {
        locallyBanned = LocallyBanned
        dailyInfluence = dailyInfluenceSource
        influence = UserInfluence
        lastKnownNickname = Nickname
        userWrapper = User
    }

    fun hasAuthority(authorityLevel: AuthorityLevel): Boolean {
        return authority.hasAuthority(authorityLevel)
    }

    init {
        lastKnownNickname = guild?.getMemberById(userId)?.nickname
    }

    val Member.wrapper: MemberWrapper
        get() = bot.getMemberWrapper(id, guild.id)
}