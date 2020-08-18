package botmanager.frostbalance

import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.PlayerCharacter

class Player(var networkId: String, @Transient var userWrapper: UserWrapper) : Containable<UserWrapper> {

    val character: PlayerCharacter
        get() = gameNetwork.worldMap.getCharacter(this)

    var allegiance //guild this player has allegiance to
            : Nation? = null

    val guild: GuildWrapper?
        get() = gameNetwork.guildWithAllegiance(allegiance)

    val gameNetwork: GameNetwork
        get() = userWrapper.bot.getGameNetwork(networkId)

    /**
     * Gets this player as though it were a member of the guild it has its allegiance to.
     * @return null if the guild doesn't exist, or if the user isn't in the guild.
     */
    val member: MemberWrapper?
        get() = gameNetwork.guildWithAllegiance(allegiance)?.let { userWrapper.memberIn(it) }


    val name: String
        get() = member?.effectiveName ?: userWrapper.name

    override fun setParent(parent: UserWrapper) {
        this.userWrapper = parent
    }


}