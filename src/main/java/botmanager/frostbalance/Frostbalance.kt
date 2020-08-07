package botmanager.frostbalance

import botmanager.IOUtils
import botmanager.Utilities
import botmanager.Utils
import botmanager.frostbalance.GuildWrapper
import botmanager.frostbalance.Nation
import botmanager.frostbalance.OptionFlag
import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.command.AuthorityLevel
import botmanager.frostbalance.command.FrostbalanceCommandBase
import botmanager.frostbalance.commands.admin.*
import botmanager.frostbalance.commands.influence.*
import botmanager.frostbalance.commands.map.ClaimTileCommand
import botmanager.frostbalance.commands.map.GetClaimsCommand
import botmanager.frostbalance.commands.map.MoveCommand
import botmanager.frostbalance.commands.map.ViewMapCommand
import botmanager.frostbalance.commands.meta.*
import botmanager.frostbalance.data.RegimeData
import botmanager.frostbalance.data.TerminationCondition
import botmanager.frostbalance.grid.WorldMap
import botmanager.frostbalance.menu.Menu
import botmanager.generic.BotBase
import botmanager.generic.ICommand
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.internal.managers.GuildManagerImpl
import net.dv8tion.jda.internal.utils.tuple.Pair
import java.awt.AlphaComposite
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class Frostbalance(botToken: String?, name: String?) : BotBase(botToken, name) {
    var userWrappers: MutableList<UserWrapper> = ArrayList()
    private val guildWrappers: MutableList<GuildWrapper> = ArrayList()
    var mainMap: WorldMap? = null
    var regimes: Map<Guild?, MutableList<RegimeData>?> = HotMap()
    private val activeMenus: MutableList<Menu> = ArrayList()
    private val guildIconCache: MutableList<Guild> = ArrayList()
    private val saverTimer = Timer()
    private fun load() {
        try {
            loadUsers()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        try {
            loadGuilds()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        try {
            loadMaps()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun shutdown() {
        saveMaps()
        saveUsers()
        saveGuilds()
    }

    val prefix: String
        get() = "."

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        for (command in commands) {
            command.run(event)
        }
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        for (command in commands) {
            command.run(event)
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        var targetMenu: Menu
        for (menu in getActiveMenus()) {
            if (event.user == menu.actor) {
                try {
                    if (menu.message.id == event.messageId) {
                        targetMenu = menu
                        targetMenu.applyResponse(event.reactionEmote)
                        break
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    //these happen often enough. let's not have them ruin other menus when they do
                }
            }
        }
    }

    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) {
        var targetMenu: Menu? = null
        for (menu in getActiveMenus()) {
            if (event.user == menu.actor) {
                if (menu.message.id == event.messageId) {
                    targetMenu = menu
                    break
                }
            }
        }
        targetMenu?.applyResponse(event.reactionEmote)
    }

    override fun onGuildUpdateIcon(event: GuildUpdateIconEvent) {
        if (guildIconCache(event.guild)) return
        val urlString = event.newIconUrl
        val guildFlags = getSettings(event.guild)
        if (urlString == null) {
            val iconNameToUse: String
            iconNameToUse = if (!guildFlags.contains(OptionFlag.MAIN)) {
                "icon_tweak/snowflake.png"
            } else if (guildFlags.contains(OptionFlag.RED)) {
                "icon_tweak/snowflake_r.png"
            } else if (guildFlags.contains(OptionFlag.GREEN)) {
                "icon_tweak/snowflake_g.png"
            } else if (guildFlags.contains(OptionFlag.BLUE)) {
                "icon_tweak/snowflake_b.png"
            } else {
                "icon_tweak/snowflake_w.png"
            }
            val iconToUse = javaClass.classLoader.getResource(iconNameToUse)
            try {
                val defaultIcon = Icon.from(File(iconToUse.file))
                GuildManagerImpl(event.guild).setIcon(defaultIcon).queue()
                return
            } catch (e: IOException) {
                System.err.println("Cannot put in the default guild icon: the file " + iconToUse + "didn't load correctly!")
                e.printStackTrace()
            }
        } else if (guildFlags.contains(OptionFlag.MAIN)) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "")
            } catch (e: IOException) {
                System.err.println("Cannot update the new guild icon!")
                e.printStackTrace()
                guildIconCache(event.guild)
                return
            }
            try {
                val effectToUse = javaClass.classLoader.getResource("icon_tweak/effect.png")
                val baseImage = ImageIO.read(connection!!.inputStream)
                val effect = ImageIO.read(File(effectToUse.file))

                //FIXME cause this to work on smaller images
                //FIXME increase image intensity
                run {
                    val effectChanges = effect.createGraphics()
                    effectChanges.scale(baseImage.width.toFloat() / effect.width as Double, baseImage.height.toFloat() / effect.height as Double)
                    effectChanges.composite = AlphaComposite.SrcAtop
                    effectChanges.color = getGuildColor(event.guild)
                    effectChanges.fillRect(0, 0, effect.width, effect.height)
                    effectChanges.dispose()
                }
                val g = baseImage.createGraphics()
                g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)
                g.drawImage(effect, (baseImage.width - effect.width) / 2,
                        (baseImage.height - effect.height) / 2, null)
                g.dispose()
                val os = ByteArrayOutputStream()
                ImageIO.write(baseImage, "png", os)
                val icon = Icon.from(os.toByteArray())
                GuildManagerImpl(event.guild).setIcon(icon).queue()
                return
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        guildIconCache(event.guild)
    }

    private fun guildIconCache(guild: Guild): Boolean {
        return if (guildIconCache.contains(guild)) {
            guildIconCache.remove(guild)
            true
        } else {
            guildIconCache.add(guild)
            false
        }
    }

    private fun getActiveMenus(): List<Menu> {
        return activeMenus
    }

    fun addMenu(menu: Menu) {
        activeMenus.add(menu)
    }

    fun removeMenu(menu: Menu) {
        activeMenus.remove(menu)
    }

    override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
        println("PLAYER LEAVING: " + event.user.id)
        if (getOwnerId(event.guild) == event.user.id) {
            println("Leader left, making a note here")
            endRegime(event.guild, TerminationCondition.LEFT)
        }
        try {
            event.guild.retrieveBan(event.user).complete() //verify this player was banned and didn't just leave
            if (hasDiplomatStatus(event.user)
                    && !isBanned(event.guild, event.user)
                    && getSettings(event.guild).contains(OptionFlag.MAIN)) {
                event.guild.unban(event.user).complete()
                Utilities.sendGuildMessage(event.guild.defaultChannel,
                        event.user.name + " has been unbanned because they are the leader of a main server.")
            }
        } catch (e: ErrorResponseException) {
            //nothing, there was no ban.
            //TODO ask for permission, not forgiveness
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        println("PLAYER JOINING: " + event.user.id)
        if (isBanned(event.guild, event.user)) {
            println("Found a banned player, banning them once again")
            event.guild.ban(event.user, 0, BAN_MESSAGE).queue()
        }
    }

    @Deprecated("")
    fun getUserCSVAtIndex(guild: Guild?, userId: String, index: Int): String {
        val guildId: String
        guildId = guild?.id ?: "global"
        val file = File("data/$name/$guildId/$userId.csv")
        return if (!file.exists()) {
            ""
        } else Utilities.getCSVValueAtIndex(Utilities.read(file), index)
    }

    @Deprecated("")
    fun setUserCSVAtIndex(guild: Guild?, user: User, index: Int, newValue: String?) {
        val guildId: String
        guildId = guild?.id ?: "global"
        val file = File("data/" + name + "/" + guildId + "/" + user.id + ".csv")
        val data = Utilities.read(file)
        val originalValues = data.split(",".toRegex()).toTypedArray()
        val newValues: Array<String?>
        if (originalValues.size > index) {
            newValues = data.split(",".toRegex()).toTypedArray()
        } else {
            newValues = arrayOfNulls(index + 1)
            System.arraycopy(originalValues, 0, newValues, 0, originalValues.size)
            for (i in originalValues.size until newValues.size) {
                newValues[i] = ""
            }
        }
        newValues[index] = newValue
        Utilities.write(file, Utilities.buildCSV(newValues))
    }

    private val adminIds: List<String>
        private get() = Utilities.readLines(File("data/$name/staff.csv"))

    @Deprecated("")
    fun loadRecords(guild: Guild?) {
        val info = Utilities.readLines(File("data/" + name + "/" + guild!!.id + "/history.csv"))
        if (info != null && !info.isEmpty()) {
            for (line in info) {
                if (line.isEmpty()) {
                    regimes.getOrDefault(guild, ArrayList())
                    continue
                }
                val rulerId = Utilities.getCSVValueAtIndex(line, 0)
                val lastKnownUserName = Utilities.getCSVValueAtIndex(line, 1)
                var startDay: Long
                startDay = try {
                    Utilities.getCSVValueAtIndex(line, 2).toLong()
                } catch (e: NumberFormatException) {
                    0
                }
                var endDay: Long
                endDay = try {
                    Utilities.getCSVValueAtIndex(line, 3).toLong()
                } catch (e: NumberFormatException) {
                    0
                }
                var terminationCondition: TerminationCondition
                terminationCondition = try {
                    TerminationCondition.valueOf(Utilities.getCSVValueAtIndex(line, 4))
                } catch (e: IllegalArgumentException) {
                    TerminationCondition.UNKNOWN
                } catch (e: NullPointerException) {
                    TerminationCondition.UNKNOWN
                }
                regimes.getOrDefault(guild, ArrayList())!!.add(RegimeData(getGuildWrapper(guild.id), rulerId, startDay, endDay, terminationCondition))
            }
        }
    }

    @Deprecated("")
    fun hasBeenForciblyRemoved(member: Member): Boolean {
        val relevantRegimes: List<RegimeData>? = getRecords(member.guild)
        return try {
            val lastRegime = relevantRegimes!![relevantRegimes.size - 1]
            if (lastRegime.terminationCondition === TerminationCondition.RESET && lastRegime.userId == member.user.id) {
                false
            } else true
        } catch (e: IndexOutOfBoundsException) {
            true
        }
    }

    @Deprecated("")
    fun banUser(guild: Guild, user: User) {
        setUserCSVAtIndex(guild, user, 1, java.lang.Boolean.TRUE.toString())
        try {
            guild.ban(user, 0).queue()
        } catch (e: HierarchyException) {
            System.err.println("Unable to ban admin user " + user.name + ".")
            e.printStackTrace()
        }
    }

    /**
     *
     * @param guild
     * @param user
     * @return Whether the user had a ban from the server when pardoned.
     */
    @Deprecated("")
    fun pardonUser(guild: Guild, user: User): Boolean {
        setUserCSVAtIndex(guild, user, 1, java.lang.Boolean.FALSE.toString())
        try {
            guild.unban(user).queue()
        } catch (e: ErrorResponseException) {
            return false
        }
        return true
    }

    @Deprecated("")
    fun globallyBanUser(user: User) {
        Utilities.append(File("data/$name/global/bans.csv"), user.id)
        for (guild in jda.guilds) {
            if (guild.isMember(user)) {
                try {
                    guild.ban(user, 0).queue()
                } catch (e: HierarchyException) {
                    System.err.println("Unable to fully ban user " + user.name + " because they have admin privileges in some servers!")
                    e.printStackTrace()
                }
            }
        }
    }

    @Deprecated("")
    fun globallyPardonUser(user: User): Boolean {
        val file = File("data/$name/global/bans.csv")
        var found = false
        val validBans: MutableList<String> = ArrayList()
        for (line in Utilities.readLines(file)) {
            if (line != user.id) {
                validBans.add(line)
            } else {
                found = true
                for (guild in jda.guilds) {
                    try {
                        guild.unban(user).queue()
                    } catch (e: ErrorResponseException) {
                        //nothing
                    }
                }
            }
        }
        Utilities.write(file, java.lang.String.join("\n", validBans))
        return found
    }

    /**
     * Returns if the player is banned from this guild.
     * @param guild The guild to check
     * @param user The user to check
     * @return Whether this user is banned from this guild, or banned globally
     */
    @Deprecated("")
    fun isBanned(guild: Guild?, user: User?): Boolean {
        return isGloballyBanned(user) || isLocallyBanned(guild, user)
    }

    /**
     * Returns if the player is banned from this guild.
     * @param guild The guild to check
     * @param user The user to check
     * @return Whether this user is banned from this guild, or banned globally
     */
    @Deprecated("")
    fun isLocallyBanned(guild: Guild?, user: User?): Boolean {
        return java.lang.Boolean.parseBoolean(getUserCSVAtIndex(guild, user!!.id, 1))
    }

    /**
     * Returns if the player is globally banned.
     * This function is expensive and should not be fired often.
     * @param user The user to check
     * @return Whether this user is banned globally
     */
    @Deprecated("")
    fun isGloballyBanned(user: User?): Boolean {
        val bannedUserIds = Utilities.readLines(File("data/$name/global/bans.csv"))
        for (bannedUserId in bannedUserIds) {
            if (bannedUserId == user!!.id) {
                return true
            }
        }
        return false
    }

    @Deprecated("")
    private fun getRecords(guild: Guild?): MutableList<RegimeData>? {
        if (regimes[guild] == null) {
            loadRecords(guild)
        }
        return regimes.getOrDefault(guild, ArrayList())
    }

    @Deprecated("")
    fun readRecords(guild: Guild?): List<RegimeData> {
        if (regimes[guild] == null) {
            loadRecords(guild)
        }
        return if (regimes[guild] == null) {
            ArrayList()
        } else {
            ArrayList(regimes[guild])
        }
    }

    @Deprecated("")
    fun updateLastRegime(guild: Guild, regime: RegimeData) {
        Utilities.removeLine(File("data/" + name + "/" + guild.id + "/history.csv"))
        Utilities.append(File("data/" + name + "/" + guild.id + "/history.csv"), regime.toCSV())
    }

    @Deprecated("")
    fun logRegime(guild: Guild, regime: RegimeData) {
        Utilities.append(File("data/" + name + "/" + guild.id + "/history.csv"), regime.toCSV())
    }

    @Deprecated("")
    fun endRegime(guild: Guild, condition: TerminationCondition?) {
        val regimeData = getRecords(guild)
        val currentOwnerId = getOwnerId(guild)
        val currentOwner = getOwner(guild)
        if (currentOwner != null) {
            guild.removeRoleFromMember(currentOwner, getOwnerRole(guild)!!).queue()
        }
        if (currentOwnerId != null && !currentOwnerId.isEmpty()) {
            println("Ending active regime of $currentOwnerId")
            try {
                val lastRegimeIndex = regimeData!!.size - 1
                regimeData[lastRegimeIndex].end(condition)
                updateLastRegime(guild, regimeData[lastRegimeIndex])
            } catch (e: IndexOutOfBoundsException) {
                System.err.println("Index out of bounds when trying to adjust the last regime! The history data may be lost.")
                System.err.println("Creating a fragmented history.")
                val regime = RegimeData(getGuildWrapper(guild.id), currentOwnerId)
                regime.end(condition)
                regimeData!!.add(regime)
                logRegime(guild, regime)
            }
            removeOwner(guild)
        }
    }

    /**
     * Returns true if the player is a leader in a main server.
     * @param user The user in question
     * @return True if a guild can be found where this player is the same as the owner of that server.
     */
    @Deprecated("")
    fun hasDiplomatStatus(user: User): Boolean {
        for (guild in jda.guilds) {
            if (getSettings(guild).contains(OptionFlag.MAIN) && getOwner(guild)!!.user == user) {
                return true
            }
        }
        return false
    }

    @Deprecated("")
    fun startRegime(guild: Guild, user: User) {
        val regime = RegimeData(getGuildWrapper(guild.id), user.id, Utilities.todayAsLong())
        getRecords(guild)!!.add(regime)
        guild.addRoleToMember(guild.getMember(user)!!, getOwnerRole(guild)!!).queue()
        updateOwner(guild, user)
        logRegime(guild, regime)
    }

    @Deprecated("")
    fun getSettings(guild: Guild): Collection<OptionFlag> {
        val debugFlags: MutableCollection<OptionFlag> = HashSet()
        val flags = Utilities.readLines(File("data/" + name + "/" + guild.id + "/flags.csv"))
        for (flag in flags) {
            debugFlags.add(OptionFlag.valueOf(flag!!))
        }
        return debugFlags
    }

    /**
     * Flips a flag for a guild.
     * @param guild
     * @param toggledFlag
     * @return Whether the debug flag got turned on (TRUE) or off (FALSE)
     */
    @Deprecated("")
    fun flipFlag(guild: Guild, toggledFlag: OptionFlag): Boolean {
        return if (getSettings(guild).contains(toggledFlag)) {
            removeDebugFlag(guild, toggledFlag)
            false
        } else {
            for (previousFlag in getSettings(guild)) {
                if (previousFlag.isExclusiveWith(toggledFlag)) {
                    removeDebugFlag(guild, previousFlag)
                }
            }
            addDebugFlag(guild, toggledFlag)
            true
        }
    }

    @Deprecated("")
    fun addDebugFlag(guild: Guild, debugFlag: OptionFlag) {
        val file = File("data/" + name + "/" + guild.id + "/flags.csv")
        Utilities.append(file, debugFlag.toString())
    }

    @Deprecated("")
    fun removeDebugFlag(guild: Guild, debugFlag: OptionFlag) {
        val file = File("data/" + name + "/" + guild.id + "/flags.csv")
        val lines = Utilities.readLines(file)
        val i = lines.iterator()
        while (i.hasNext()) {
            val line = i.next()
            if (debugFlag == OptionFlag.valueOf(line)) {
                i.remove()
                break
            }
        }
        Utilities.write(file, "")
        for (line in lines) {
            Utilities.append(file, line)
        }
    }

    @Deprecated("")
    fun getOwnerId(guild: Guild?): String {
        val info = Utilities.read(File("data/" + name + "/" + guild!!.id + "/owner.csv"))
        return Utilities.getCSVValueAtIndex(info, 0)
    }

    @Deprecated("")
    fun getOwner(guild: Guild): Member? {
        return try {
            val info = Utilities.read(File("data/" + name + "/" + guild.id + "/owner.csv"))
            guild.getMember(jda.getUserById(Utilities.getCSVValueAtIndex(info, 0))!!)
        } catch (e: NullPointerException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @Deprecated("")
    fun updateOwner(guild: Guild, user: User) {
        Utilities.write(File("data/" + name + "/" + guild.id + "/owner.csv"), user.id)
    }

    @Deprecated("")
    fun removeOwner(guild: Guild) {
        Utilities.removeFile(File("data/" + name + "/" + guild.id + "/owner.csv"))
    }

    @Deprecated("")
    fun changeUserInfluence(guild: Guild?, user: User, influence: Influence) {
        val startingInfluence = getUserInfluence(guild, user)
        var newInfluence = influence.add(startingInfluence)
        if (newInfluence.getThousandths() < 0) {
            newInfluence = Influence(0)
        }
        setUserCSVAtIndex(guild, user, 0, newInfluence.toString())
    }

    @Deprecated("")
    fun changeUserInfluence(member: Member, influence: Influence) {
        changeUserInfluence(member.guild, member.user, influence)
    }

    @Deprecated("")
    fun gainDailyInfluence(member: Member, influenceGained: Influence): Influence {
        var influenceGained = influenceGained
        return if (getUserLastDaily(member) != Utilities.todayAsLong()) { //new day
            setUserLastDaily(member, Utilities.todayAsLong())
            setUserDailyAmount(member, influenceGained)
            changeUserInfluence(member, influenceGained)
            influenceGained
        } else if (getUserDailyAmount(member).add(influenceGained).compareTo(DailyInfluenceSource.DAILY_INFLUENCE_CAP) <= 0) { //cap doesn't affect anything
            setUserDailyAmount(member, getUserDailyAmount(member).add(influenceGained))
            changeUserInfluence(member, influenceGained)
            influenceGained
        } else { //influence gained is over the cap
            influenceGained = DailyInfluenceSource.DAILY_INFLUENCE_CAP.subtract(getUserDailyAmount(member)) //set it to the cap before doing anything
            setUserDailyAmount(member, getUserDailyAmount(member).add(influenceGained))
            changeUserInfluence(member, influenceGained)
            influenceGained
        }
    }

    @Deprecated("")
    fun getUserInfluence(guild: Guild?, user: User?): Influence {
        return try {
            Influence(getUserCSVAtIndex(guild, user!!.id, 0).toDouble())
        } catch (e: NumberFormatException) {
            Influence(0)
        }
    }

    @Deprecated("")
    fun getUserInfluence(member: Member): Influence {
        return getUserInfluence(member.guild, member.user)
    }

    @Deprecated("")
    fun getUserDailyAmount(guild: Guild?, user: User?): Influence {
        return try {
            Influence(getUserCSVAtIndex(guild, user!!.id, 3).toDouble())
        } catch (e: NumberFormatException) {
            Influence(0)
        }
    }

    @Deprecated("")
    fun getUserDailyAmount(member: Member): Influence {
        return getUserDailyAmount(member.guild, member.user)
    }

    @Deprecated("")
    fun setUserDailyAmount(guild: Guild?, user: User, amount: Influence) {
        setUserCSVAtIndex(guild, user, 3, amount.toString())
    }

    @Deprecated("")
    fun setUserDailyAmount(member: Member, amount: Influence) {
        setUserDailyAmount(member.guild, member.user, amount)
    }

    @Deprecated("")
    fun getUserLastDaily(guild: Guild?, user: User?): Long {
        return try {
            getUserCSVAtIndex(guild, user!!.id, 2).toInt().toLong()
        } catch (e: NumberFormatException) {
            0
        }
    }

    @Deprecated("")
    fun getUserLastDaily(member: Member): Long {
        return getUserLastDaily(member.guild, member.user)
    }

    @Deprecated("")
    fun setUserLastDaily(guild: Guild?, user: User, date: Long) {
        setUserCSVAtIndex(guild, user, 2, date.toString())
    }

    @Deprecated("")
    fun setUserLastDaily(member: Member, date: Long) {
        setUserLastDaily(member.guild, member.user, date)
    }

    @Deprecated("")
    fun resetUserDefaultGuild(user: User) {
        setUserCSVAtIndex(null, user, 0, "")
    }

    @Deprecated("")
    fun setUserDefaultGuild(user: User, guild: Guild) {
        setUserCSVAtIndex(null, user, 0, guild.id)
    }

    @Deprecated("")
    fun setMainAllegiance(user: User, nation: Nation) {
        setUserCSVAtIndex(null, user, 1, nation.toString())
    }

    @Deprecated("")
    fun getMainAllegiance(user: User): Nation {
        val allegiance = getUserCSVAtIndex(null, user.id, 1)
        return if (Utils.isNullOrEmpty(allegiance)) Nation.NONE else Nation.valueOf(allegiance)
    }

    @Deprecated("")
    fun getUserDefaultGuild(user: User?): Guild? {
        return try {
            jda.getGuildById(getUserCSVAtIndex(null, user!!.id, 0))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun loadLegacy() {
        loadUsersFromCSV()
        loadMembersFromCSV()
        loadGuildsFromCSV()
    }

    override fun getCommands(): Array<FrostbalanceCommandBase> {
        val commands = super.getCommands()
        val newCommands = arrayOfNulls<FrostbalanceCommandBase>(commands.size)
        for (i in commands.indices) {
            newCommands[i] = commands[i] as FrostbalanceCommandBase
        }
        return newCommands.requireNoNulls()
    }

    fun getOwnerRole(guild: Guild): Role? {
        return try {
            guild.getRolesByName("LEADER", true)[0]
        } catch (e: IndexOutOfBoundsException) {
            System.err.println(guild.name + " doesn't have a valid owner role!")
            null
        }
    }

    fun getSystemRole(guild: Guild): Role? {
        return try {
            guild.getRolesByName("FROSTBALANCE", true)[0]
        } catch (e: IndexOutOfBoundsException) {
            System.err.println(guild.name + " doesn't have a valid frostbalance role!")
            null
        }
    }

    /**
     * Performs a soft reset on a guild. This will set reset all player roles and lift all bans.
     * In the future, it will also reset the server icon and name.
     * It will *not* reset player data about influence, leader history, channels or their conversations.
     * @param guild The guild to reset.
     */
    fun softReset(guild: Guild) {
        val roles = guild.roles
        for (role in roles) {
            if (getSystemRole(guild) != role && getOwnerRole(guild) != role) {
                role.delete()
            }
        }
        //TODO don't unban players who are under a global ban.
        for (ban in guild.retrieveBanList().complete()) {
            guild.unban(ban.user).queue()
        }
    }
    //FIXME perform functions different than the soft reset.
    /**
     * Performs a hard reset on a guild. This will set reset all player roles and lift all bans,
     * reset the server name and icon, delete all stored data about a server and its members, and delete
     * all channels and their conversations, leaving only a general channel.
     * It will *not* reset player data about influence, leader history, channels or their conversations.
     * @param guild The guild to reset.
     */
    fun hardReset(guild: Guild) {
        softReset(guild)
    }

    fun getAuthority(user: User): AuthorityLevel {
        return if (this.jda.selfUser.id == user.id) {
            AuthorityLevel.BOT
        } else if (adminIds.contains(user.id)) {
            AuthorityLevel.BOT_ADMIN
        } else {
            AuthorityLevel.GENERIC
        }
    }

    /**
     * Returns how much authority a user has in a given context.
     * @param guild The server they are operating in
     * @param user The user that is operating
     * @return The authority level of the user
     */
    fun getAuthority(guild: Guild?, user: User): AuthorityLevel {
        if (guild == null) return getAuthority(user)
        return if (this.jda.selfUser.id == user.id) {
            AuthorityLevel.BOT
        } else if (adminIds.contains(user.id)) {
            AuthorityLevel.BOT_ADMIN
        } else if (guild.owner!!.user.id == user.id) {
            AuthorityLevel.GUILD_OWNER
        } else if (guild.getMember(user)!!.roles.contains(getSystemRole(guild))) {
            AuthorityLevel.GUILD_ADMIN
        } else if (guild.getMember(user)!!.roles.contains(getOwnerRole(guild))) {
            AuthorityLevel.SERVER_LEADER
        } else if (guild.getMember(user)!!.hasPermission(Permission.ADMINISTRATOR)) {
            AuthorityLevel.SERVER_ADMIN
        } else {
            AuthorityLevel.GENERIC
        }
    }

    fun getAuthority(member: Member): AuthorityLevel {
        return getAuthority(member.guild, member.user)
    }

    fun getAllegianceIn(guild: Guild): Nation {
        val flags = getSettings(guild)
        if (!flags.contains(OptionFlag.MAIN)) {
            return Nation.NONE
        }
        return if (flags.contains(OptionFlag.RED)) {
            Nation.RED
        } else if (flags.contains(OptionFlag.GREEN)) {
            Nation.GREEN
        } else if (flags.contains(OptionFlag.BLUE)) {
            Nation.BLUE
        } else {
            Nation.NONE
        }
    }

    @Deprecated("")
    fun getGuildColor(guild: Guild): Color {
        val flags = getSettings(guild)
        return if (flags.contains(OptionFlag.RED)) {
            Color.RED
        } else if (flags.contains(OptionFlag.GREEN)) {
            Color.GREEN
        } else if (flags.contains(OptionFlag.BLUE)) {
            Color.BLUE
        } else {
            Color.LIGHT_GRAY
        }
    }

    /**
     *
     * @param nation
     * @return Null if no guild exists for this nation
     */
    fun getGuildFor(nation: Nation): Guild? {
        for (guild in jda.guilds) {
            if (getSettings(guild).contains(OptionFlag.MAIN)) {
                if (nation == getAllegianceIn(guild)) {
                    return guild
                }
            }
        }
        return null
    }

    fun saveMaps() {
        for (map in WorldMap.getMaps()) {
            WorldMap.writeWorld(map.guild, map)
        }
    }

    //FIXME add a flag for whether the maps have been loaded - there's a risk of losing all map data if someone is fast enough
    //TODO test if this ever loads a subset of the guilds the bot is a part of, as that would leave a subset of the maps actually loaded.
    fun loadMaps() {
        if (jda.guilds.isEmpty()) {
            val exec = ScheduledThreadPoolExecutor(1)
            exec.schedule({ loadMaps() }, 1, TimeUnit.SECONDS)
        } else {
            for (guild in jda.guilds) {
                if (!getSettings(guild).contains(OptionFlag.MAIN)) {
                    println("Loading map for " + guild.name)
                    WorldMap.readWorld(guild.id)
                }
            }
            mainMap = loadMainMap()
        }
    }

    private fun loadMainMap(): WorldMap {
        return WorldMap.readWorld(null)
    }

    fun saveUsers() {
        for (user in userWrappers) {
            writeObject("users/" + user.userId, user)
        }
    }

    private fun saveGuilds() {
        for (guild in guildWrappers) {
            writeObject("guilds/" + guild.id, guild)
        }
    }

    fun loadGuildsFromCSV() {
        for (folder in File("data/$name").listFiles()) {
            println("Folder:$folder")
            if (!folder.isDirectory) continue
            val guildId = folder.name
            println("GuildId:$guildId")
            try {
                val guild = jda.getGuildById(guildId) ?: continue
                println("Guild:$guild")
                try {
                    val bGuild = getGuildWrapper(guildId)
                    bGuild.loadLegacy(
                            getSettings(Objects.requireNonNull(bGuild.guild)!!) as MutableSet<OptionFlag>,
                            getRecords(bGuild.guild)!!,
                            getOwnerId(bGuild.guild),
                            bGuild.guild!!.name
                    )
                    guildWrappers.add(bGuild)
                    println("Added guild:" + bGuild.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: NumberFormatException) {
            }
        }
    }

    fun loadUsersFromCSV() {
        for (file in File("data/$name/global").listFiles()) {
            val fileName = file.name
            if (!fileName.contains(".csv")) continue
            val userId = fileName.replace(".csv", "")
            println("UserId: $userId")
            try {
                if (jda.getUserById(userId) != null) {
                    val bUser = getUserWrapper(userId)
                    println("User: " + bUser.user)
                    try {
                        bUser.legacyLoad(
                                getMainAllegiance(Objects.requireNonNull(bUser.user)!!),
                                getUserDefaultGuild(bUser.user)!!.id,
                                isGloballyBanned(bUser.user),
                                bUser.user!!.name,
                                adminIds.contains(bUser.userId)
                        )
                        userWrappers.add(bUser)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println("Added: " + bUser.name)
                }
            } catch (e: NumberFormatException) {
            }
        }
    }

    fun loadMembersFromCSV() {
        for (folder in File("data/$name").listFiles()) {
            println("Folder:$folder")
            if (!folder.isDirectory) continue
            val guildId = folder.name
            println("GuildId:$guildId")
            try {
                val guild = jda.getGuildById(guildId) ?: continue
                for (file in folder.listFiles()) {
                    val fileName = file.name
                    if (!fileName.contains(".csv")) continue
                    val userId = fileName.replace(".csv", "")
                    val user = jda.getUserById(userId)
                    try {
                        if (jda.getUserById(userId) != null) {
                            val bMember = getMemberWrapper(userId, guildId)
                            bMember.loadLegacy(isLocallyBanned(guild, user),
                                    DailyInfluenceSource(
                                            getUserDailyAmount(guild, user),
                                            getUserLastDaily(guild, user)
                                    ),
                                    getUserInfluence(guild, user),
                                    guild.getMember(user!!)!!.nickname,
                                    getUserWrapper(userId))
                        }
                    } catch (e: NumberFormatException) {
                    }
                }
            } catch (e: NumberFormatException) {
            }
        }
    }

    fun loadUsers() {
        for (file in File("data/$name/users").listFiles()) {
            if (file.exists()) {
                val gsonBuilder = GsonBuilder()
                val gson = gsonBuilder.create()
                val userWrapper = gson.fromJson(IOUtils.read(file), UserWrapper::class.java)
                userWrapper.load(this)
                if (userWrapper.userId != null) { //impossible condition test
                    userWrappers.add(userWrapper)
                } else {
                    Thread.dumpStack()
                }
            }
        }
    }

    fun loadGuilds() {
        for (file in File("data/$name/guilds").listFiles()) {
            if (file.exists()) {
                val gsonBuilder = GsonBuilder()
                val gson = gsonBuilder.create()
                val guildWrapper = gson.fromJson(IOUtils.read(file), GuildWrapper::class.java)
                guildWrapper.load(this)
                if (guildWrapper.id != null) { //impossible condition test
                    guildWrappers.add(guildWrapper)
                } else {
                    Thread.dumpStack()
                }
            }
        }
    }

    fun writeObject(filename: String, `object`: Any?, vararg typeAdapters: Pair<Class<*>?, TypeAdapter<*>?>) {
        val file = File("data/$name/$filename.json")
        val gsonBuilder = GsonBuilder()
        for (typeAdapterPair in typeAdapters) {
            gsonBuilder.registerTypeAdapter(typeAdapterPair.left, typeAdapterPair.right)
        }
        val gson = gsonBuilder.setPrettyPrinting().create()
        IOUtils.write(file, gson.toJson(`object`))
    }

    fun getUserName(userId: String?): String {
        return jda.getUserById(userId!!)!!.name
    }

    @Deprecated("")
    fun gainDailyInfluence(member: Member): Influence {
        return gainDailyInfluence(member, DailyInfluenceSource.DAILY_INFLUENCE_CAP)
    }

    /**
     * Gets the bot user for this player.
     * If none exist, this player will be given a new bot user.
     * @param id
     * @return
     */
    fun getUserWrapper(id: String): UserWrapper {
        var botUser = userWrappers.stream().filter { user: UserWrapper -> user.userId == id }.findFirst()
        if (!botUser.isPresent) {
            val user = jda.getUserById(id)
            botUser = if (user == null) {
                Optional.of(UserWrapper(this, id))
            } else {
                Optional.of(UserWrapper(this, user))
            }
            userWrappers.add(botUser.get())
        }
        return botUser.get()
    }

    /**
     * Gets the bot member for this user in this guild.
     * If none exist, this player will be given a new bot member.
     * @param userId
     * @param guildId
     * @return
     */
    fun getMemberWrapper(userId: String, guildId: String?): MemberWrapper {
        val userWrapper = getUserWrapper(userId)
        return userWrapper.getMember(guildId!!)
    }

    /**
     * Gets the bot guild for this guild.
     * If none exist, this guild will be given a new bot guild.
     * @param id
     * @return
     */
    fun getGuildWrapper(id: String): GuildWrapper {
        Objects.requireNonNull(id)
        var botGuild = guildWrappers.stream().filter { guild: GuildWrapper ->
            println(guild.id)
            guild.id == id
        }.findFirst()
        if (!botGuild.isPresent) {
            val guild = jda.getGuildById(id)
            botGuild = if (guild == null) {
                Optional.of(GuildWrapper(this, id))
            } else {
                Optional.of(GuildWrapper(this, guild))
            }
            guildWrappers.add(botGuild.get())
        }
        return botGuild.get()
    }

    fun getUserByName(targetName: String): UserWrapper? {
        for (userWrapper in userWrappers) {
            if (userWrapper.name == targetName) {
                return userWrapper
            }
        }
        return null
    }

    companion object {
        lateinit var bot: Frostbalance
        private const val BAN_MESSAGE = "You have been banned system-wide by a staff member. Either you have violated Discord's TOS or you have been warned before about some violation of Frostbalance rules. If you believe this is in error, get in touch with a staff member."
    }

    init {
        bot = this
        jda.presence.activity = Activity.of(Activity.ActivityType.DEFAULT, prefix + "help for help!")
        setCommands(arrayOf<ICommand>(
                HelpCommand(this),
                ImplicitInfluence(this),
                DailyRewardCommand(this),
                GetInfluenceCommand(this),
                SupportCommand(this),
                OpposeCommand(this),
                CheckCommand(this),
                CoupCommand(this),
                InaugurateCommand(this),
                HistoryCommand(this),
                SetGuildCommand(this),
                InterveneCommand(this),
                AdjustCommand(this),
                SystemBanCommand(this),
                SystemPardonCommand(this),
                FlagCommand(this),
                ViewMapCommand(this),
                ClaimTileCommand(this),
                AllegianceCommand(this),
                MoveCommand(this),
                GetClaimsCommand(this),
                LoadLegacyCommand(this)
        ))
        load()
        saverTimer.schedule(object : TimerTask() {
            override fun run() {
                saveMaps()
                saveUsers()
                saveGuilds()
            }
        }, 300000, 300000)
    }
}