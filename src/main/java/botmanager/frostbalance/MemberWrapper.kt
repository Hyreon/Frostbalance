package botmanager.frostbalance

import botmanager.frostbalance.grid.Containable
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member

class MemberWrapper(@Transient var userWrapper: UserWrapper, var guildId: String) : Containable<UserWrapper?> {


    private var lastKnownNickname: String?

    var influence: Influence = Influence(0)
    var dailyInfluence = DailyInfluenceSource()
    var banned = false
    private val userId: String
        get() = userWrapper.userId

    fun adjustInfluence(amount: Influence?) {
        influence = influence.add(amount)
        if (influence.isNegative) {
            influence = Influence.none()
        }
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
     * @return The member if extant, or an empty optional if the bot has been removed from the relevant guild,
     * or the relevant player has left from it.
     */
    val member: Member?
        get() = jda.getGuildById(guildId)?.getMemberById(userId)
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

    fun loadLegacy(LocallyBanned: Boolean, dailyInfluenceSource: DailyInfluenceSource, UserInfluence: Influence, Nickname: String, User: UserWrapper) {
        banned = LocallyBanned
        dailyInfluence = dailyInfluenceSource
        influence = UserInfluence
        lastKnownNickname = Nickname
        userWrapper = User
    }

    init {
        lastKnownNickname = guild?.getMemberById(userId)?.nickname
    }

    val Member.wrapper: MemberWrapper
        get() = bot.getMemberWrapper(id, guild.id)
}