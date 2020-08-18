package botmanager.frostbalance

import botmanager.IOUtils
import botmanager.Utilities
import botmanager.Utils
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
import botmanager.frostbalance.grid.Container
import botmanager.frostbalance.grid.TileObject
import botmanager.frostbalance.grid.TileObjectAdapter
import botmanager.frostbalance.grid.WorldMap
import botmanager.frostbalance.menu.Menu
import botmanager.generic.BotBase
import botmanager.generic.ICommand
import com.google.gson.GsonBuilder
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

    private val gameNetworks: MutableList<GameNetwork> = ArrayList()
    private val userWrappers: MutableList<UserWrapper> = ArrayList()

    var mainNetwork: GameNetwork
        get() = gameNetworks[0]
        set(it) = {
            gameNetworks.remove(it)
            gameNetworks.add(0, it)
        }.invoke()

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
            loadGames()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    override fun shutdown() {
        saveUsers()
        saveGames()
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
        getActiveMenus().firstOrNull { menu ->
            event.userId == menu.actor.id && menu.message.id == event.messageId
        }?.applyResponse(event.reactionEmote)
    }

    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) {
        getActiveMenus().firstOrNull { menu ->
            event.userId == menu.actor.id && menu.message.id == event.messageId
        }?.applyResponse(event.reactionEmote)
    }

    override fun onGuildUpdateIcon(event: GuildUpdateIconEvent) {
        if (guildIconCache(event.guild)) return
        val urlString = event.newIconUrl
        val guildFlags = getSettings(event.guild)
        if (urlString == null) {
            val iconNameToUse: String
            iconNameToUse = if (!guildFlags.contains(OldOptionFlag.MAIN)) {
                "icon_tweak/snowflake.png"
            } else if (guildFlags.contains(OldOptionFlag.RED)) {
                "icon_tweak/snowflake_r.png"
            } else if (guildFlags.contains(OldOptionFlag.GREEN)) {
                "icon_tweak/snowflake_g.png"
            } else if (guildFlags.contains(OldOptionFlag.BLUE)) {
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
        } else if (guildFlags.contains(OldOptionFlag.MAIN)) {
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
                val baseImage = ImageIO.read(connection.inputStream)
                val effect = ImageIO.read(File(effectToUse.file))

                //FIXME cause this to work on smaller images
                //FIXME increase image intensity
                run {
                    val effectChanges = effect.createGraphics()
                    effectChanges.scale(baseImage.width.toFloat() / effect.width.toDouble(), baseImage.height.toFloat() / effect.height.toDouble())
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
                    && getSettings(event.guild).contains(OldOptionFlag.MAIN)) {
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

    private fun getUserCSVAtIndex(guild: Guild?, userId: String, index: Int): String {
        val guildId: String
        guildId = guild?.id ?: "global"
        val file = File("data/$name/$guildId/$userId.csv")
        return if (!file.exists()) {
            ""
        } else Utilities.getCSVValueAtIndex(Utilities.read(file), index)
    }

    private fun setUserCSVAtIndex(guild: Guild?, user: User, index: Int, newValue: String?) {
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
        get() = Utilities.readLines(File("data/$name/staff.csv"))

    private fun loadRecords(guild: Guild?) {
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

    /**
     * Returns if the player is banned from this guild.
     * @param guild The guild to check
     * @param user The user to check
     * @return Whether this user is banned from this guild, or banned globally
     */
    private fun isBanned(guild: Guild?, user: User?): Boolean {
        return isGloballyBanned(user) || isLocallyBanned(guild, user)
    }

    /**
     * Returns if the player is banned from this guild.
     * @param guild The guild to check
     * @param user The user to check
     * @return Whether this user is banned from this guild, or banned globally
     */
    private fun isLocallyBanned(guild: Guild?, user: User?): Boolean {
        return java.lang.Boolean.parseBoolean(getUserCSVAtIndex(guild, user!!.id, 1))
    }

    /**
     * Returns if the player is globally banned.
     * This function is expensive and should not be fired often.
     * @param user The user to check
     * @return Whether this user is banned globally
     */
    private fun isGloballyBanned(user: User?): Boolean {
        val bannedUserIds = Utilities.readLines(File("data/$name/global/bans.csv"))
        for (bannedUserId in bannedUserIds) {
            if (bannedUserId == user!!.id) {
                return true
            }
        }
        return false
    }

    private fun getRecords(guild: Guild?): MutableList<RegimeData>? {
        if (regimes[guild] == null) {
            loadRecords(guild)
        }
        return regimes.getOrDefault(guild, ArrayList())
    }

    private fun updateLastRegime(guild: Guild, regime: RegimeData) {
        Utilities.removeLine(File("data/" + name + "/" + guild.id + "/history.csv"))
        Utilities.append(File("data/" + name + "/" + guild.id + "/history.csv"), regime.toCSV())
    }

    private fun logRegime(guild: Guild, regime: RegimeData) {
        Utilities.append(File("data/" + name + "/" + guild.id + "/history.csv"), regime.toCSV())
    }

    private fun endRegime(guild: Guild, condition: TerminationCondition?) {
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
    private fun hasDiplomatStatus(user: User): Boolean {
        for (guild in jda.guilds) {
            if (getSettings(guild).contains(OldOptionFlag.MAIN) && getOwner(guild)!!.user == user) {
                return true
            }
        }
        return false
    }

    private fun getSettings(guild: Guild): Collection<OldOptionFlag> {
        val debugFlagOlds: MutableCollection<OldOptionFlag> = HashSet()
        val flags = Utilities.readLines(File("data/" + name + "/" + guild.id + "/flags.csv"))
        for (flag in flags) {
            debugFlagOlds.add(OldOptionFlag.valueOf(flag!!))
        }
        return debugFlagOlds
    }

    private fun addDebugFlag(guild: Guild, debugFlagOld: OldOptionFlag) {
        val file = File("data/" + name + "/" + guild.id + "/flags.csv")
        Utilities.append(file, debugFlagOld.toString())
    }

    private fun removeDebugFlag(guild: Guild, debugFlagOld: OldOptionFlag) {
        val file = File("data/" + name + "/" + guild.id + "/flags.csv")
        val lines = Utilities.readLines(file)
        val i = lines.iterator()
        while (i.hasNext()) {
            val line = i.next()
            if (debugFlagOld == OldOptionFlag.valueOf(line)) {
                i.remove()
                break
            }
        }
        Utilities.write(file, "")
        for (line in lines) {
            Utilities.append(file, line)
        }
    }

    private fun getOwnerId(guild: Guild?): String {
        val info = Utilities.read(File("data/" + name + "/" + guild!!.id + "/owner.csv"))
        return Utilities.getCSVValueAtIndex(info, 0)
    }

    private fun getOwner(guild: Guild): Member? {
        return try {
            val info = Utilities.read(File("data/" + name + "/" + guild.id + "/owner.csv"))
            guild.getMember(jda.getUserById(Utilities.getCSVValueAtIndex(info, 0))!!)
        } catch (e: NullPointerException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun updateOwner(guild: Guild, user: User) {
        Utilities.write(File("data/" + name + "/" + guild.id + "/owner.csv"), user.id)
    }

    private fun removeOwner(guild: Guild) {
        Utilities.removeFile(File("data/" + name + "/" + guild.id + "/owner.csv"))
    }

    private fun changeUserInfluence(guild: Guild?, user: User, influence: Influence) {
        val startingInfluence = getUserInfluence(guild, user)
        var newInfluence = influence.add(startingInfluence)
        if (newInfluence.getThousandths() < 0) {
            newInfluence = Influence(0)
        }
        setUserCSVAtIndex(guild, user, 0, newInfluence.toString())
    }

    private fun changeUserInfluence(member: Member, influence: Influence) {
        changeUserInfluence(member.guild, member.user, influence)
    }

    private fun gainDailyInfluence(member: Member, influenceGained: Influence): Influence {
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

    private fun getUserInfluence(guild: Guild?, user: User?): Influence {
        return try {
            Influence(getUserCSVAtIndex(guild, user!!.id, 0).toDouble())
        } catch (e: NumberFormatException) {
            Influence(0)
        }
    }

    private fun getUserDailyAmount(guild: Guild?, user: User?): Influence {
        return try {
            Influence(getUserCSVAtIndex(guild, user!!.id, 3).toDouble())
        } catch (e: NumberFormatException) {
            Influence(0)
        }
    }

    private fun getUserDailyAmount(member: Member): Influence {
        return getUserDailyAmount(member.guild, member.user)
    }

    private fun setUserDailyAmount(guild: Guild?, user: User, amount: Influence) {
        setUserCSVAtIndex(guild, user, 3, amount.toString())
    }

    private fun setUserDailyAmount(member: Member, amount: Influence) {
        setUserDailyAmount(member.guild, member.user, amount)
    }

    private fun getUserLastDaily(guild: Guild?, user: User?): Long {
        return try {
            getUserCSVAtIndex(guild, user!!.id, 2).toInt().toLong()
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun getUserLastDaily(member: Member): Long {
        return getUserLastDaily(member.guild, member.user)
    }

    private fun setUserLastDaily(guild: Guild?, user: User, date: Long) {
        setUserCSVAtIndex(guild, user, 2, date.toString())
    }

    private fun setUserLastDaily(member: Member, date: Long) {
        setUserLastDaily(member.guild, member.user, date)
    }

    private fun getMainAllegiance(user: User): Nation? {
        val allegiance = getUserCSVAtIndex(null, user.id, 1)
        return if (Utils.isNullOrEmpty(allegiance)) null else Nation.valueOf(allegiance)
    }

    private fun getUserDefaultGuild(user: User?): Guild? {
        return try {
            jda.getGuildById(getUserCSVAtIndex(null, user!!.id, 0))
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun loadLegacy() {
        loadUsersFromCSV()
        loadMembersFromCSV()
        loadGamesFromCSV()
        loadGuildsFromCSV()
        loadMapsLegacy()
        loadPlayersFromCSV()
    }

    override fun getCommands(): Array<FrostbalanceCommandBase> {
        val commands = super.getCommands()
        val newCommands = arrayOfNulls<FrostbalanceCommandBase>(commands.size)
        for (i in commands.indices) {
            newCommands[i] = commands[i] as FrostbalanceCommandBase
        }
        return newCommands.requireNoNulls()
    }

    private fun getOwnerRole(guild: Guild): Role? {
        return try {
            guild.getRolesByName("LEADER", true)[0]
        } catch (e: IndexOutOfBoundsException) {
            System.err.println(guild.name + " doesn't have a valid owner role!")
            null
        }
    }

    private fun getSystemRole(guild: Guild): Role? {
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
    private fun softReset(guild: Guild) {
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

    private fun getAuthority(user: User): AuthorityLevel {
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
    private fun getAuthority(guild: Guild?, user: User): AuthorityLevel {
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
            AuthorityLevel.NATION_LEADER
        } else if (guild.getMember(user)!!.hasPermission(Permission.ADMINISTRATOR)) {
            AuthorityLevel.NATION_ADMIN
        } else {
            AuthorityLevel.GENERIC
        }
    }

    private fun getAllegianceIn(guild: Guild): Nation? {
        val flags = getSettings(guild)
        if (!flags.contains(OldOptionFlag.MAIN)) {
            return null
        }
        return if (flags.contains(OldOptionFlag.RED)) {
            Nation.RED
        } else if (flags.contains(OldOptionFlag.GREEN)) {
            Nation.GREEN
        } else if (flags.contains(OldOptionFlag.BLUE)) {
            Nation.BLUE
        } else {
            null
        }
    }

    private fun getGuildColor(guild: Guild): Color {
        val flags = getSettings(guild)
        return when {
            flags.contains(OldOptionFlag.RED) -> {
                Color.RED
            }
            flags.contains(OldOptionFlag.GREEN) -> {
                Color.GREEN
            }
            flags.contains(OldOptionFlag.BLUE) -> {
                Color.BLUE
            }
            else -> {
                Color.GRAY
            }
        }
    }

    /**
     *
     * @param nation
     * @return Null if no guild exists for this nation
     */
    fun getGuildFor(nation: Nation): GuildWrapper? {
        return mainNetwork.guildWithAllegiance(nation)
    }

    //FIXME add a flag for whether the maps have been loaded - there's a risk of losing all map data if someone is fast enough
    //TODO test if this ever loads a subset of the guilds the bot is a part of, as that would leave a subset of the maps actually loaded.
    private fun loadMapsLegacy() {
        if (jda.guilds.isEmpty()) {
            val exec = ScheduledThreadPoolExecutor(1)
            exec.schedule({ loadMapsLegacy() }, 1, TimeUnit.SECONDS)
        } else {
            for (guild in jda.guilds) {
                if (!getSettings(guild).contains(OldOptionFlag.MAIN)) {
                    println("Loading map for " + guild.name)
                    WorldMap.readWorld(guild.id)
                }
            }
            WorldMap.readWorld(null)
        }
    }

    fun saveUsers() {
        for (user in userWrappers) {
            writeObject("users/" + user.id, user)
        }
    }

    private fun saveGames() {
        for (network in gameNetworks) {
            writeObject("games/" + network.id, network, Pair(TileObject::class.java, TileObjectAdapter()))
        }
    }

    private fun loadGamesFromCSV() {
        gameNetworks.add(GameNetwork(this, "global"))
        gameNetworks.add(GameNetwork(this, "tutorial"))
    }

    private fun loadGuildsFromCSV() {
        for (folder in File("data/$name").listFiles()) {
            println("Folder:$folder")
            if (!folder.isDirectory) continue
            val guildId = folder.name
            println("GuildId:$guildId")
            try {
                val guild = jda.getGuildById(guildId) ?: continue
                println("Guild:$guild")
                try {
                    val settings = getSettings(guild)
                    val gameNetwork = when {
                        settings.contains(OldOptionFlag.MAIN) -> {
                            getGameNetwork("global")
                        }
                        settings.contains(OldOptionFlag.TUTORIAL) -> {
                            getGameNetwork("tutorial")
                        }
                        else -> {
                            getGameNetwork(guild.id)
                        }
                    }
                    val bGuild = getGuildIfPresent(guildId) ?: GuildWrapper(gameNetwork, guild)
                    bGuild.loadLegacy(
                            getSettings(guild) as MutableSet<OldOptionFlag>,
                            getRecords(guild)!!,
                            getOwnerId(guild),
                            guild.name
                    )
                    gameNetwork.addGuild(bGuild)
                    gameNetwork.adopt()
                    println("Added guild:" + bGuild.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: NumberFormatException) {
            }
        }
    }

    private fun loadUsersFromCSV() {
        for (file in File("data/$name/global").listFiles()) {
            val fileName = file.name
            if (!fileName.contains(".csv")) continue
            val userId = fileName.replace(".csv", "")
            println("UserId: $userId")
            try {
                if (jda.getUserById(userId) != null) {
                    val bUser = getUserWrapper(userId)
                    println("User: " + bUser.jdaUser)
                    try {
                        bUser.loadLegacy(
                                getUserDefaultGuild(bUser.jdaUser)!!.id,
                                isGloballyBanned(bUser.jdaUser),
                                bUser.jdaUser!!.name,
                                adminIds.contains(bUser.id)
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

    private fun loadPlayersFromCSV() {
        for (file in File("data/$name/global").listFiles()) {
            val fileName = file.name
            if (!fileName.contains(".csv")) continue
            val userId = fileName.replace(".csv", "")
            println("UserId: $userId")
            try {
                if (jda.getUserById(userId) != null) {
                    val bUser = getUserWrapper(userId)
                    println("User: " + bUser.jdaUser)
                    try {
                        gameNetworks.forEach { gameNetwork -> bUser.playerIn(gameNetwork).allegiance = getMainAllegiance(bUser.jdaUser!!) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println("Added: " + bUser.name)
                }
            } catch (e: NumberFormatException) {
            }
        }
    }

    private fun loadMembersFromCSV() {
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

    private fun loadUsers() {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(Container::class.java, ContainerAdapter())
        val gson = gsonBuilder.create()
        println(gson.getAdapter(Container::class.java))
        for (file in File("data/$name/users").listFiles()) {
            if (file.exists()) {
                val userWrapper = gson.fromJson(IOUtils.read(file), UserWrapper::class.java)
                userWrapper.setParent(this)
                userWrapper.adopt() //TODO auto-adopt, which is what containerAdapter should be doing
                //but it's not doing its job
                //so >:(
                if (userWrapper.id != null) { //impossible condition test
                    userWrappers.add(userWrapper)
                } else {
                    Thread.dumpStack()
                }
            }
        }
    }

    private fun loadGames() {
        for (file in File("data/$name/games").listFiles()) {
            if (file.exists()) {
                val gsonBuilder = GsonBuilder()
                gsonBuilder.registerTypeAdapter(TileObject::class.java, TileObjectAdapter())
                gsonBuilder.registerTypeAdapter(Container::class.java, ContainerAdapter())
                val gson = gsonBuilder.create()
                val worldMap = gson.fromJson(IOUtils.read(file), WorldMap::class.java)
                val gameNetwork = gson.fromJson(IOUtils.read(file), GameNetwork::class.java)
                gameNetwork.setParent(this)
                gameNetwork.adopt()
                if (gameNetwork.id != null) { //impossible condition test
                    gameNetworks.add(gameNetwork)
                } else {
                    Thread.dumpStack()
                }
            }
        }
    }

    private fun writeObject(filename: String, `object`: Any?, vararg typeAdapters: Pair<Class<TileObject>, TileObjectAdapter>) {
        val file = File("data/$name/$filename.json")
        val gsonBuilder = GsonBuilder()
        for (typeAdapterPair in typeAdapters) {
            gsonBuilder.registerTypeAdapter(typeAdapterPair.first, typeAdapterPair.second)
        }
        val gson = gsonBuilder.setPrettyPrinting().create()
        IOUtils.write(file, gson.toJson(`object`))
    }

    /**
     * Gets the bot user for this player.
     * If none exist, this player will be given a new bot user.
     * @param id
     * @return
     */
    fun getUserWrapper(id: String): UserWrapper {
        var botUser = userWrappers.stream().filter { user: UserWrapper -> user.id == id }.findFirst()
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
    fun getMemberWrapper(userId: String, guildId: String): MemberWrapper {
        val userWrapper = getUserWrapper(userId)
        return userWrapper.memberIn(guildId)
    }

    /**
     * Gets the bot guild for this guild.
     * If none exist, this guild will be given a new bot guild.
     * @param id
     * @return
     */
    fun getGuildWrapper(id: String): GuildWrapper {
        Objects.requireNonNull(id)
        gameNetworks.forEach{ network ->
            network.associatedGuilds.firstOrNull{ guild -> guild.id == id }?.let {
                return it
            }
        }

        //no guild found in any network
        var network = getGameNetwork(id)
        var guild = GuildWrapper(network, id)
        network.addGuild(guild)
        return guild
    }


    fun getGuildIfPresent(id: String): GuildWrapper? {
        Objects.requireNonNull(id)
        gameNetworks.forEach{ network ->
            network.associatedGuilds.firstOrNull{ guild -> guild.id == id }?.let {
                return it
            }
        }
        return null
    }


    fun getGameNetwork(id: String): GameNetwork {
        Objects.requireNonNull(id)
        var gameNetwork = gameNetworks.firstOrNull { gameInstance ->
            println(gameInstance.id)
            gameInstance.id == id
        }
        if (gameNetwork == null) {
            gameNetwork = GameNetwork(this, id)
            gameNetworks.add(gameNetwork)
        }
        return gameNetwork
    }

    fun getUserByName(targetName: String): UserWrapper? {
        return userWrappers.firstOrNull { user -> user.name == targetName }
    }

    /**
     * Returns a newly created (and therefore safe instance) of
     * all guilds containing this map.
     */
    fun getNetworkByMap(map: WorldMap) : GameNetwork {
        return gameNetworks.first { network -> network.worldMap == map }
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
                saveGames()
                saveUsers()
            }
        }, 300000, 300000)
    }
}