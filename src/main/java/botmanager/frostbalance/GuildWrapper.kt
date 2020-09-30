package botmanager.frostbalance

import botmanager.Utilities
import botmanager.frostbalance.Frostbalance.Companion.bot
import botmanager.frostbalance.flags.GuildOptions
import botmanager.frostbalance.records.RegimeData
import botmanager.frostbalance.records.TerminationCondition
import botmanager.frostbalance.flags.OldOptionFlag
import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.Container
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

    var guildOptions = GuildOptions(this)

    val members: Collection<MemberWrapper>
        get() = bot.userWrappers.mapNotNull { user -> user.memberIfIn(this) }

    @Transient
    var guildIcon: BufferedImage? = null

    @JvmField
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

    val jdaGuild: Guild?
        get() = jda.getGuildById(id)
    val jda: JDA
        get() = bot.jda
    val color: Color
        get() = nation.color

    override fun toString() : String = if (isOnline()) name!! else name?.let { "~~$it~~" }?:"*Inaccessible Guild*"

    override fun adopt() {
        regimes.forEach { regime -> regime.setParent(this) }
        if (guildOptions == null) {
            guildOptions = GuildOptions(this)
        }
        guildOptions.setParent(this)
    }

    fun isOnline(): Boolean {
        return jdaGuild != null
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

    fun markLeaderAsDeserter() {
        endRegime(TerminationCondition.LEFT)
    }

    private fun endRegime(condition: TerminationCondition) {
        check(jdaGuild != null) { "Tried to end the regime of a server the bot isn't in!" }
        leaderAsMember?.let { jdaGuild?.removeRoleFromMember(it, leaderRole)?.queue() }
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
        jdaGuild?.addRoleToMember(member.id, leaderRole)?.queue()
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
        val roles = jdaGuild!!.roles
        for (role in roles) {
            if (systemRole != role && leaderRole != role) {
                role.delete()
            }
        }
        for (ban in jdaGuild!!.retrieveBanList().complete()) {
            if (!bot.getUserWrapper(ban.user.id).memberIn(id).banned)
            jdaGuild!!.unban(ban.user)
        }
    }

    val leaderRole: Role
        get() = {
            if (jdaGuild == null) {
                throw IllegalStateException("Tried to get the leader role in a non-existent guild!")
            }
            var roles = jdaGuild!!.getRolesByName("LEADER", true)
            if (roles.isEmpty()) {
                System.err.println("$name doesn't have a valid leader role! Attempting to create...")
                val role = jdaGuild!!.createRole()
                        .setColor(color)
                        .setName("Leader")
                        .setPermissions(Permission.ADMINISTRATOR)
                        .setHoisted(true)
                        .complete()
                jdaGuild!!.modifyRolePositions()
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
            if (jdaGuild == null) {
                throw IllegalStateException("Tried to get the leader role in a non-existent guild!")
            }
            var roles = jdaGuild!!.getRolesByName("FROSTBALANCE", true)
            if (roles.isEmpty()) {
                System.err.println("$name doesn't have a valid system role! Attempting to create...")
                val role = jdaGuild!!.createRole()
                        .setColor(color)
                        .setName("Leader")
                        .setPermissions(Permission.ALL_PERMISSIONS - Permission.ADMINISTRATOR.offset - Permission.BAN_MEMBERS.offset)
                        .setHoisted(true)
                        .complete()
                jdaGuild!!.addRoleToMember(jda.selfUser.id, role).queue()
                jdaGuild!!
                        .modifyRolePositions()
                        .selectPosition(role)
                        .moveTo(jdaGuild!!.roles.size - 1)
                        .queue()
                role
            } else {
                roles[0]
            }
        }.invoke()

    val leaderAsMember: Member?
        get() = leaderId?.let { bot.getMemberWrapper(it, id).jdaMember }

    companion object {
        val Guild.wrapper: GuildWrapper
            get() = bot.getGuildWrapper(id)
    }

    override fun setParent(parent: GameNetwork) {
        gameNetwork = parent
    }

    fun moveToNetwork(selectedNetwork: GameNetwork) {
        gameNetwork.moveGuild(this, selectedNetwork)
    }

    fun commandContextFooter(): String {
        return "Command executed in $name"
    }

    fun grantedContextFooter(): String {
        return "From $name"
    }

    fun getMemberByName(name: String): MemberWrapper? {
        return members.firstOrNull { member -> member.effectiveName.equals(name, ignoreCase = true)}
    }

    fun allows(player: Player?): Boolean {
        return player?.allegiance.let {
            guildOptions.openBordersWith(it) || it == nation
        }
    }

    val notAllowed: String
        get() {
            val initial = ":passport_control: $name has closed its borders."
            val exceptions = if (guildOptions.borderTreaties!!.isNotEmpty()) {
                "You cannot gain influence unless your allegiance is to one of these nations: ${toString()}, ${
                    guildOptions.borderTreaties!!.joinToString(", ") {
                        gameNetwork.guildWithAllegiance(it).toString()
                    }
                }"
            } else {
                "You cannot gain influence unless you have allegiance here."
            }
            return "$initial $exceptions"
        }

    private val MutableSet<OldOptionFlag>.legacyNation: Nation?
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