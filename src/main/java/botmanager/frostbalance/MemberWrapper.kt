package botmanager.frostbalance

import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.grid.Containable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException

class MemberWrapper(@Transient var userWrapper: UserWrapper, var guildId: String) : Containable<UserWrapper> {


    val authority: AuthorityLevel
        get() = bot.getAuthority(guildWrapper.guild!!, userWrapper.jdaUser!!)
    private var lastKnownNickname: String?

    var influence: Influence = Influence(0)
    var dailyInfluence = DailyInfluenceSource()
    var locallyBanned = false
    val banned: Boolean
        get() = userWrapper.globallyBanned || locallyBanned
    private val userId: String
        get() = userWrapper.id

    /**
     * @return The amount of influence that was NOT spent on the member wrapper.
     */
    fun adjustInfluence(amount: Influence?): Influence {
        influence = influence.add(amount)
        if (influence.isNegative) {
            var amountSaved = influence.negate()
            influence = Influence.none()
            return amountSaved
        }
        return Influence.none()
    }

    fun gainDailyInfluence(): Influence {
        return gainDailyInfluence(DailyInfluenceSource.DAILY_INFLUENCE_CAP)
    }

    fun gainDailyInfluence(influenceRequested: Influence): Influence {
        val influenceGained = dailyInfluence.yield(influenceRequested)
        influence = influence.add(influenceGained)
        return influenceGained
    }

    val effectiveName: String
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

    val guildWrapper: GuildWrapper
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

    fun pardon(): Boolean {
        locallyBanned = false
        try {
            userWrapper.jdaUser?.let {guildWrapper.guild?.unban(it)?.queue() ?: return false} ?: return false
        } catch (e: ErrorResponseException) {
            return false
        }
        return true
    }

    fun ban() {
        locallyBanned = true
        try {
            userWrapper.jdaUser?.let { guildWrapper.guild?.ban(it, 0)?.queue() }
        } catch (e: HierarchyException) {
            System.err.println("Unable to ban admin user $effectiveName.")
            e.printStackTrace()
        }
    }

    init {
        lastKnownNickname = guild?.getMemberById(userId)?.nickname
    }

    val Member.wrapper: MemberWrapper
        get() = bot.getMemberWrapper(id, guild.id)

    override fun setParent(parent: UserWrapper) {
        userWrapper = parent
    }
}