package botmanager.frostbalance

import botmanager.Utilities
import botmanager.frostbalance.data.RegimeData
import botmanager.frostbalance.data.TerminationCondition
import botmanager.frostbalance.grid.WorldMap
import lombok.Getter
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*

class GuildWrapper(@field:Transient @field:Getter var bot: Frostbalance, guild: Guild) {

    @Getter
    var guildId: String = guild.id

    @Transient
    var guildIcon: BufferedImage? = null

    @JvmField
    @Getter
    var lastKnownName: String? = null

    var leaderId: String? = null
    var map: WorldMap? = null
    @JvmField
    var optionFlags: Set<OptionFlag> = HashSet()
    @JvmField
    var regimes: MutableList<RegimeData> = ArrayList()
    val name: String?
        get() {
            if (jda.getGuildById(guildId) != null) {
                lastKnownName = jda.getGuildById(guildId)!!.name
            }
            return lastKnownName
        }
    val guild: Guild?
        get() = jda.getGuildById(guildId)
    val jda: JDA
        get() = bot.jda
    val color: Color
        get() = if (optionFlags.contains(OptionFlag.RED)) {
            Color.RED
        } else if (optionFlags.contains(OptionFlag.GREEN)) {
            Color.GREEN
        } else if (optionFlags.contains(OptionFlag.BLUE)) {
            Color.BLUE
        } else {
            Color.LIGHT_GRAY
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

    fun doCoup(user: User) {
        endRegime(TerminationCondition.COUP)
        startRegime(user)
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

    private fun startRegime(user: User) {
        val regime = RegimeData(this, user.id, Utilities.todayAsLong())
        regimes.add(regime)
        guild?.addRoleToMember(user.id, leaderRole)?.queue()
        leaderId = user.id
    }

    fun load(frostbalance: Frostbalance) {
        bot = frostbalance
        for (regimeData in regimes) {
            regimeData.guildWrapper = this
        }
    }

    fun loadLegacy(Settings: MutableSet<OptionFlag>, Records: MutableList<RegimeData>, OwnerId: String, Name: String) {
        optionFlags = Settings
        regimes = Records
        leaderId = OwnerId
        lastKnownName = Name
    }

    private val leaderRole: Role
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

    private val systemRole: Role
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

    private val leaderAsMember: Member?
        get() = bot.getMemberWrapper(leaderId, guildId).member

    val Guild.wrapper: GuildWrapper
        get() = bot.getGuildWrapper(id)
}