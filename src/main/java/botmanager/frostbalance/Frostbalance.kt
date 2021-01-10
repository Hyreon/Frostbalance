package botmanager.frostbalance

import botmanager.utils.IOUtils
import botmanager.Utilities
import botmanager.utils.Utils
import botmanager.frostbalance.GuildWrapper.Companion.wrapper
import botmanager.frostbalance.action.ActionQueue
import botmanager.frostbalance.action.ActionQueueAdapter
import botmanager.frostbalance.action.QueueStep
import botmanager.frostbalance.action.QueueStepAdapter
import botmanager.frostbalance.command.FrostbalanceCommand
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.commands.admin.*
import botmanager.frostbalance.commands.influence.*
import botmanager.frostbalance.commands.map.*
import botmanager.frostbalance.commands.meta.*
import botmanager.frostbalance.records.RegimeData
import botmanager.frostbalance.records.TerminationCondition
import botmanager.frostbalance.flags.OldOptionFlag
import botmanager.frostbalance.grid.Container
import botmanager.frostbalance.grid.TileObject
import botmanager.frostbalance.grid.TileObjectAdapter
import botmanager.frostbalance.grid.WorldMap
import botmanager.frostbalance.menu.Menu
import botmanager.generic.BotBase
import com.google.gson.GsonBuilder
import com.google.gson.JsonSerializer
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.update.GuildUpdateIconEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.internal.managers.GuildManagerImpl
import java.awt.AlphaComposite
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
    internal val userWrappers: MutableList<UserWrapper> = ArrayList()

    val networkList: List<GameNetwork>
        get() = gameNetworks.toList()

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
        super.shutdown();
        saveUsers()
        saveGames()
    }

    val prefix: String
        get() = "."

    //TODO run commands asynchronously so that it can wait for user input on some commands
    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        for (command in commands) {
            command.run(event)
        }
        for (menu in activeMenus.filter { menu -> menu.hasHook }) {
            menu.hook!!.readMessage(MessageContext(this, event))
        }
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        for (command in commands) {
            command.run(event)
        }
        for (menu in activeMenus.filter { menu -> menu.hasHook }) {
            menu.hook!!.readMessage(MessageContext(this, event))
        }
    }

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        getActiveMenus().firstOrNull { menu ->
            event.userId == menu.actor?.id && menu.message?.id == event.messageId
        }?.applyResponse(event.reactionEmote)
    }

    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) {
        getActiveMenus().firstOrNull { menu ->
            event.userId == menu.actor?.id && menu.message?.id == event.messageId
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
                    effectChanges.color = event.guild.wrapper.color
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

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        println("PLAYER LEAVING: " + event.user.id)
        if (event.guild.wrapper.leaderId == event.user.id) {
            println("Leader left, making a note here")
            event.guild.wrapper.markLeaderAsDeserter()
        }
        try {
            event.guild.retrieveBan(event.user).complete() //verify this player was banned and didn't just leave
            if (event.user.wrapper.playerIn(event.guild.wrapper.gameNetwork).isLeader
                    && !event.user.wrapper.memberIn(event.guild.wrapper).banned
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
        if (getUserWrapper(event.user.id).memberIn(event.guild.id).banned) {
            println("Found a banned player, banning them once again")
            event.guild.ban(event.user, 0, BAN_MESSAGE).queue()
        }
    }

    private fun getUserCSVAtIndex(guild: Guild?, userId: String, index: Int): String {
        val guildId: String = guild?.id ?: "global"
        val file = File("data/$name/$guildId/$userId.csv")
        return if (!file.exists()) {
            ""
        } else Utilities.getCSVValueAtIndex(Utilities.read(file), index)
    }

    private fun setUserCSVAtIndex(guild: Guild?, user: User, index: Int, newValue: String?) {
        val guildId: String = guild?.id ?: "global"
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
        if (info != null && info.isNotEmpty()) {
            for (line in info) {
                if (line.isEmpty()) {
                    regimes.getOrDefault(guild, ArrayList())
                    continue
                }
                val rulerId = Utilities.getCSVValueAtIndex(line, 0)
                var startDay: Long
                startDay = try {
                    Utilities.getCSVValueAtIndex(line, 2).toLong()
                } catch (e: NumberFormatException) {
                    0
                }
                var endDay: Long = try {
                    Utilities.getCSVValueAtIndex(line, 3).toLong()
                } catch (e: NumberFormatException) {
                    0
                }
                var terminationCondition: TerminationCondition = try {
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

    private fun getSettings(guild: Guild): Collection<OldOptionFlag> {
        val debugFlagOlds: MutableCollection<OldOptionFlag> = HashSet()
        val flags = Utilities.readLines(File("data/" + name + "/" + guild.id + "/flags.csv"))
        for (flag in flags) {
            debugFlagOlds.add(OldOptionFlag.valueOf(flag!!))
        }
        return debugFlagOlds
    }

    private fun getOwnerId(guild: Guild?): String {
        val info = Utilities.read(File("data/" + name + "/" + guild!!.id + "/owner.csv"))
        return Utilities.getCSVValueAtIndex(info, 0)
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

    private fun getUserLastDaily(guild: Guild?, user: User?): Long {
        return try {
            getUserCSVAtIndex(guild, user!!.id, 2).toInt().toLong()
        } catch (e: NumberFormatException) {
            0
        }
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

    override fun getCommands(): Array<FrostbalanceCommand> {
        val commands = super.getCommands()
        val newCommands = arrayOfNulls<FrostbalanceCommand>(commands.size)
        for (i in commands.indices) {
            newCommands[i] = commands[i] as FrostbalanceCommand
        }
        return newCommands.requireNoNulls()
    }

    /**
     *
     * @param nation
     * @return Null if no guild exists for this nation
     */
    fun getGuildFor(nation: Nation): GuildWrapper? {
        return mainNetwork.guildWithAllegiance(nation)
    }

    private fun loadMapsLegacy() {
        if (jda.guilds.isEmpty()) {
            val exec = ScheduledThreadPoolExecutor(1)
            exec.schedule({ loadMapsLegacy() }, 1, TimeUnit.SECONDS)
        } else {
            for (guild in jda.guilds) {
                if (!getSettings(guild).contains(OldOptionFlag.MAIN)) {
                    println("Loading map for " + guild.name)
                    try {
                        WorldMap.readWorldLegacy(guild.id)
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                        continue
                    }
                }
            }
            WorldMap.readWorldLegacy(null)
        }
    }

    fun saveUsers() {
        for (user in userWrappers) {
            writeObject("users/" + user.id, user)
        }
    }

    private fun saveGames() {
        for (network in gameNetworks) {
            writeObject("games/" + network.id, network, Pair(
                    QueueStep::class.java, QueueStepAdapter()), Pair(TileObject::class.java, TileObjectAdapter()))
            //TODO if network.isEmpty() remove file
        }
    }

    private fun loadGamesFromCSV() {
        gameNetworks.clear()
        gameNetworks.add(GameNetwork(this, "main")) //establishes "main" as the first (main) network
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
                            getGameNetwork("main")
                        }
                        else -> {
                            getGameNetwork(guild.id)
                        }
                    }
                    val bGuild = getGuildIfPresent(guildId) ?: GuildWrapper(gameNetwork, guild)
                    gameNetwork.addGuild(bGuild)
                    gameNetwork.adopt()
                    gameNetwork.initializeTurnCycle()
                    bGuild.loadLegacy(
                            getSettings(guild) as MutableSet<OldOptionFlag>,
                            getRecords(guild)!!,
                            getOwnerId(guild),
                            guild.name
                    )
                    println("Added guild:" + bGuild.name)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: NumberFormatException) {
            }
        }
    }

    private fun loadUsersFromCSV() {
        userWrappers.clear()
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
                        gameNetworks.forEach { gameNetwork -> bUser.playerIn(gameNetwork).writeAllegiance(getMainAllegiance(bUser.jdaUser!!)) }
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
                                    guild.getMember(user!!)?.nickname,
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
                gsonBuilder.registerTypeAdapter(QueueStep::class.java, QueueStepAdapter())
                gsonBuilder.registerTypeAdapter(Container::class.java, ContainerAdapter())
                gsonBuilder.registerTypeAdapter(ActionQueue::class.java, ActionQueueAdapter())
                val gson = gsonBuilder.create()
                val gameNetwork = gson.fromJson(IOUtils.read(file), GameNetwork::class.java)
                gameNetwork.setParent(this)
                gameNetwork.adopt()
                gameNetwork.initializeTurnCycle()
                if (gameNetwork.id != null) { //impossible condition test
                    gameNetworks.add(gameNetwork)
                    if (gameNetwork.id == "main") {
                        gameNetwork.setAsMain()
                    }
                } else {
                    Thread.dumpStack()
                }
            }
        }
    }

    private fun writeObject(filename: String, `object`: Any?, vararg typeAdapters: Pair<Class<out Any>, JsonSerializer<out Any>>) {
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
            gameInstance.id == id
        }
        if (gameNetwork == null) {
            gameNetwork = GameNetwork(this, id)
            gameNetworks.add(gameNetwork)
        }
        return gameNetwork
    }

    fun getUserByName(targetName: String, guild: GuildWrapper?): UserWrapper? {
        return guild?.let { userWrappers.firstOrNull { user -> user.memberIfIn(it)?.effectiveName == targetName }}
                ?: userWrappers.firstOrNull { user -> println(user.name); user.name == targetName }
    }

    companion object {
        lateinit var bot: Frostbalance
        private const val BAN_MESSAGE = "You have been banned system-wide by a staff member. Either you have violated Discord's TOS or you have been warned before about some violation of Frostbalance rules. If you believe this is in error, get in touch with a staff member."
    }

    init {
        bot = this
        jda.presence.activity = Activity.of(Activity.ActivityType.DEFAULT, prefix + "help for help!")
        setCommands(arrayOf(
                HelpCommand(this),
                DailyRewardCommand(this),
                SubscribeCommand(this),
                UnsubscribeCommand(this),
                ImplicitSubscription(this),
                ImplicitInfluence(this),
                GetInfluenceCommand(this),
                SupportCommand(this),
                OpposeCommand(this),
                CheckCommand(this),
                CoupCommand(this),
                InaugurateCommand(this),
                BanCommand(this),
                PardonCommand(this),
                HistoryCommand(this),
                SetGuildCommand(this),
                InterveneCommand(this),
                AdjustCommand(this),
                SystemBanCommand(this),
                SystemPardonCommand(this),
                SettingsCommand(this),
                ViewMapCommand(this),
                ClaimTileCommand(this),
                AllegianceCommand(this),
                MoveCommand(this),
                GetClaimsCommand(this),
                GrantClaimCommand(this),
                UnclaimCommand(this),
                EvictCommand(this),
                UnevictCommand(this),
                LoadLegacyCommand(this),
                TopClaimsCommand(this),
                TriangleCommand(this),
                GarbageCommand(this)
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