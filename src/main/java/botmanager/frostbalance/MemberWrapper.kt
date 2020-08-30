package botmanager.frostbalance

import botmanager.frostbalance.Frostbalance.Companion.bot
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.grid.Containable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException
import org.jsoup.internal.StringUtil
import java.time.LocalDate

class MemberWrapper(@Transient var userWrapper: UserWrapper, var guildId: String) : Containable<UserWrapper> {

    val authority: AuthorityLevel
        get() = {
            when {
                jdaGuild?.owner?.user?.id == userId -> {
                    AuthorityLevel.GUILD_OWNER
                }
                member?.roles?.contains(guildWrapper.systemRole) ?: false -> {
                    AuthorityLevel.GUILD_ADMIN
                }
                member?.roles?.contains(guildWrapper.leaderRole) ?: false -> {
                    AuthorityLevel.NATION_LEADER
                }
                member?.hasPermission(Permission.ADMINISTRATOR) ?: false -> {
                    AuthorityLevel.NATION_ADMIN
                }
                else -> {
                    AuthorityLevel.GENERIC
                }
            }
        }.invoke().coerceAtLeast(userWrapper.authority)

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

    var subscription: WeeklyInfluenceSource? = null

    val subscribed: Boolean
        get() = subscription?.let { !it.finished } ?: false

    fun subscribe() {
        subscription = WeeklyInfluenceSource()
    }

    fun unsubscribe() {
        subscription = null
    }

    fun updateSubscription() {
        subscription?.getWeeklyInfluence(this, dailyInfluence)?.takeIf { it > 0 }?.let {
            val sub = subscription!!
            val content = mutableListOf("You have gained $it influence from your subscription in ${guildWrapper.lastKnownName}.")
            if (sub.finished) {
                if (sub.nextRequestDate < LocalDate.now().toEpochDay()) {
                    content.add("Your subscription ended on ${LocalDate.ofEpochDay(sub.nextRequestDate)}. Gain influence again with `.subscribe`.")
                } else {
                    content.add("Your subscription ended today. Gain influence again with `.subscribe`.")
                }
            }
            userWrapper.sendNotification(guildWrapper, StringUtil.join(content, "\n"))
        }
    }

    fun gainDailyInfluence(influenceRequested: Influence = DailyInfluenceSource.DAILY_INFLUENCE_CAP, date: Long? = null): Influence {
        val influenceGained = dailyInfluence.yield(influenceRequested, date)
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
    private val jdaGuild: Guild?
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
            userWrapper.jdaUser?.let {guildWrapper.jdaGuild?.unban(it)?.queue() ?: return false} ?: return false
        } catch (e: ErrorResponseException) {
            return false
        }
        return true
    }

    fun ban() {
        locallyBanned = true
        try {
            userWrapper.jdaUser?.let { guildWrapper.jdaGuild?.ban(it, 0)?.queue() }
        } catch (e: HierarchyException) {
            System.err.println("Unable to ban admin user $effectiveName.")
            e.printStackTrace()
        }
    }

    init {
        lastKnownNickname = jdaGuild?.getMemberById(userId)?.nickname
    }

    override fun setParent(parent: UserWrapper) {
        userWrapper = parent
    }
}

val Member.wrapper: MemberWrapper
    get() = bot.getMemberWrapper(id, guild.id)