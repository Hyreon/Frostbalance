package botmanager.frostbalance

import botmanager.frostbalance.grid.Containable
import botmanager.frostbalance.grid.PlayerCharacter

class Player(var networkId: String, @Transient var userWrapper: UserWrapper) : Containable<UserWrapper> {

    val character: PlayerCharacter
        get() = gameNetwork.worldMap.getCharacter(this)

    var allegiance //guild this player has allegiance to
            : Nation? = null
        set(value) {
            field = value
            gameNetwork.worldMap.loadedTiles
                    .filter { tile -> tile.claimData.hasClaim(this) }
                    .forEach { tile -> tile.claimData.updateCacheTime() }
        }

    var locallyBanned: Boolean = false

    val guild: GuildWrapper?
        get() = gameNetwork.guildWithAllegiance(allegiance)

    val gameNetwork: GameNetwork
        get() = userWrapper.bot.getGameNetwork(networkId)

    /**
     * Gets this player as though it were a member of the guild it has its allegiance to.
     * @return the member. This will throw an exception if the right guild couldn't be found.
     */
    val member: MemberWrapper
        get() = gameNetwork.guildWithAllegiance(allegiance)!!.let { userWrapper.memberIn(it) }


    val name: String
        get() = member.effectiveName ?: userWrapper.name

    val isLeader: Boolean
        get() = gameNetwork.associatedGuilds.any { guild -> guild.leaderId == userWrapper.id }

    override fun setParent(parent: UserWrapper) {
        this.userWrapper = parent
    }


}