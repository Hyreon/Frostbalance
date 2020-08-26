package botmanager.frostbalance

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance.Companion.bot
import botmanager.frostbalance.data.RegimeData
import botmanager.frostbalance.data.TerminationCondition
import botmanager.frostbalance.flags.OldOptionFlag
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.Container
import lombok.Getter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*

class GuildWrapper(@Transient var gameNetwork: GameNetwork, var id: String) : Containable<GameNetwork>, Container {


    constructor(gameNetwork: GameNetwork, guild: Guild) : this(gameNetwork, guild.id)

    @Transient
    var guildIcon: BufferedImage? = null

    @JvmField
    @Getter
    var lastKnownName: String? = null

    var leaderId: String? = null

    var nation: Nation =
            Nation.values().toMutableSet().subtract(gameNetwork.nations).randomOrNull() ?:
            throw IllegalStateException("This network is at capacity and cannot support another guild!")

    @JvmField
    var regimes: MutableList<RegimeData> = ArrayList()
    val name: String?
        get() {
            if (jda.getGuildById(id) != null) {
                lastKnownName = jda.getGuildById(id)!!.name
            }
            return lastKnownName
        }

    val guild: Guild?
        get() = jda.getGuildById(id)
    val jda: JDA
        get() = bot.jda
    val color: Color
        get() = nation.color

    override fun toString() : String = if (isOnline()) name!! else name?.let { "~~$it~~" }?:"*Inaccessible Guild*"

    override fun adopt() {
        regimes.forEach { regime -> regime.setParent(this) }
    }

    fun isOnline(): Boolean {
        return guild != null
    }

    fun getMember(user: UserWrapper): MemberWrapper {
        return user.memberIn(id)
    }

    /**
     * Returns a modifiable clone of this server's records.
     */
    fun readRecords(): List<RegimeData> {
        return ArrayList(regimes)
    }

    fun hasBeenForciblyRemoved(userId: String): Boolean {
        return regimes.size > 0 && regimes[regimes.size - 1].userId == userId && regimes[regimes.size - 1].terminationCondition == TerminationCondition.RESET
    }

    fun doCoup(member: Member) {
        endRegime(TerminationCondition.COUP)
        startRegime(member)
    }

    fun inaugurate(member: Member) {
        endRegime(TerminationCondition.TRANSFER)
        startRegime(member)
    }

    private fun endRegime(condition: TerminationCondition) {
        check(guild != null) { "Tried to end the regime of a server the bot isn't in!" }
        leaderAsMember?.let { guild?.removeRoleFromMember(it, leaderRole)?.queue() }
        if (leaderId != null) {
            println("Ending active regime of $leaderId")
            try {
                val lastRegimeIndex = regimes.size - 1
                regimes[lastRegimeIndex].end(condition)
            } catch (e: IndexOutOfBoundsException) {
                System.err.println("Index out of bounds when trying to adjust the last regime! The history data may be lost.")
                System.err.println("Creating a fragmented history.")
                val regime = RegimeData(this, leaderId)
                regime.end(condition)
                regimes.add(regime)
            }
            leaderId = null
        }
    }

    private fun startRegime(member: Member) {
        val regime = RegimeData(this, member.id, Utilities.todayAsLong())
        regimes.add(regime)
        guild?.addRoleToMember(member.id, leaderRole)?.queue()
        leaderId = member.id
    }

    fun loadLegacy(settings: MutableSet<OldOptionFlag>, Records: MutableList<RegimeData>, OwnerId: String, Name: String) {
        nation = settings.legacyNation ?: Nation.LIGHT
        regimes = Records
        leaderId = OwnerId
        lastKnownName = Name
    }

    fun reset() {
        endRegime(TerminationCondition.RESET)
        softReset()
    }

    private fun softReset() {
        val roles = guild!!.roles
        for (role in roles) {
            if (systemRole != role && leaderRole != role) {
                role.delete()
            }
        }
        //TODO don't unban players who are under a global ban.
        for (ban in guild!!.retrieveBanList().complete()) {
            if (!bot.getUserWrapper(ban.user.id).memberIn(id).banned)
            guild!!.unban(ban.user)
        }
    }

    val leaderRole: Role
        get() = {
            if (guild == null) {
                throw IllegalStateException("Tried to get the leader role in a non-existent guild!")
            }
            var roles = guild!!.getRolesByName("LEADER", true)
            if (roles.isEmpty()) {
                System.err.println("$name doesn't have a valid leader role! Attempting to create...")
                val role = guild!!.createRole()
                        .setColor(color)
                        .setName("Leader")
                        .setPermissions(Permission.ADMINISTRATOR)
                        .setHoisted(true)
                        .complete()
                guild!!.modifyRolePositions()
                        .selectPosition(role)
                        .moveTo(systemRole.position - 1)
                        .queue()
                role!!
            } else {
                roles[0]
            }
        }.invoke()

    val systemRole: Role
        get() = {
            if (guild == null) {
                throw IllegalStateException("Tried to get the leader role in a non-existent guild!")
            }
            var roles = guild!!.getRolesByName("FROSTBALANCE", true)
            if (roles.isEmpty()) {
                System.err.println("$name doesn't have a valid system role! Attempting to create...")
                val role = guild!!.createRole()
                        .setColor(color)
                        .setName("Leader")
                        .setPermissions(Permission.ADMINISTRATOR)
                        .setHoisted(true)
                        .complete()
                guild!!.addRoleToMember(jda.selfUser.id, role).queue()
                guild!!
                        .modifyRolePositions()
                        .selectPosition(role)
                        .moveTo(guild!!.roles.size - 1)
                        .queue()
                role
            } else {
                roles[0]
            }
        }.invoke()

    val leaderAsMember: Member?
        get() = leaderId?.let { bot.getMemberWrapper(it, id).member }

    companion object {
        val Guild.wrapper: GuildWrapper
            get() = bot.getGuildWrapper(id)
    }

    override fun setParent(parent: GameNetwork) {
        gameNetwork = parent
    }


    val MutableSet<OldOptionFlag>.legacyNation: Nation?
        get() = when {
            contains(OldOptionFlag.BLUE) -> {
                Nation.BLUE
            }
            contains(OldOptionFlag.RED) -> {
                Nation.RED
            }
            contains(OldOptionFlag.GREEN) -> {
                Nation.GREEN
            }
            else -> {
                null
            }
        }


}