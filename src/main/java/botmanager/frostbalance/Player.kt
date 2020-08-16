package botmanager.frostbalance

import botmanager.frostbalance.grid.PlayerCharacter

class Player(@Transient var gameNetwork: GameNetwork, var userId: String) {

    val character: PlayerCharacter
        get() = gameNetwork.worldMap.getCharacter(this)

    var defaultGuildId //guild this player has allegiance to
            : String? = null

    private val allegiance: GuildWrapper?
        get() = defaultGuildId?.let { gameNetwork.bot.getGuildWrapper(it) }

    /**
     * Gets this player as though it were a member of the guild it has its allegiance to.
     * @return null if the guild doesn't exist, or if the user isn't in the guild.
     */
    val member: MemberWrapper?
        get() = allegiance?.let { Frostbalance.bot.getUserWrapper(userId).memberIn(it) }

    val user: UserWrapper
        get() = gameNetwork.bot.getUserWrapper(userId)


    val name: String
        get() = member?.effectiveName ?: user.name


}