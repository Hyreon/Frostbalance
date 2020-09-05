package botmanager.frostbalance.grid

import botmanager.Cache
import botmanager.Utilities
import botmanager.frostbalance.Influence
import botmanager.frostbalance.MemberWrapper
import botmanager.frostbalance.Nation
import botmanager.frostbalance.Player
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

class ClaimData(tile: Tile?) : TileData(tile), Container {

    /**
     * The strength of each players' claim to a specific tile. See Claim for details.
     */
    var claims: MutableList<Claim> = ArrayList()
        get() = field

    override fun getTile(): Tile {
        return tile
    }

    //cached function: depends on state 'claims'; if 'claims' is modified since
    //the last function call or the state is null, run the function. otherwise,
    //return the last value.

    /**
     * Adds a claim with the given parameters to this tile.
     * @param character
     * @param nation
     * @param strength
     */
    private fun addClaim(player: Player, nation: Nation, strength: Influence): Claim {
        updateCacheTime()
        for (claim in claims) {
            if (claim.overlaps(this, player, nation)) {
                claim.add(strength)
                return claim
            }
        }
        return Claim(player.character, nation, strength)
    }

    fun addClaim(member: MemberWrapper, strength: Influence): Claim {
        return addClaim(member.player, member.guildWrapper.nation, strength)
    }

    fun addClaim(player: Player, strength: Influence): Claim {
        return addClaim(player, player.allegiance!!, strength)
    }

    fun addClaim(newClaim: Claim): Claim {
        updateCacheTime()
        for (claim in claims) {
            require(!newClaim.overlaps(claim)) { "This claim overlaps an existing claim!" }
        }
        claims.add(newClaim)
        return newClaim
    }

    fun getClaim(player: Player?, nation: Nation?): Claim? {
        for (claim in claims) {
            if (claim.overlaps(this, player, nation)) {
                return claim
            }
        }
        return null
    }

    fun getClaim(member: MemberWrapper): Claim? {
        return getClaim(member.userWrapper.playerIn(member.guildWrapper.gameNetwork), member.guildWrapper.nation)
    }

    fun hasClaim(player: Player): Boolean {
        return claims.any { claim -> claim.player == player }
    }

    /**
     * Reduce the strength of a claim.
     * Any player can do this to their own claims at no cost, but with no refund.
     * @return
     */
    fun reduceClaim(player: Player?, nation: Nation?, amount: Influence?): Influence {
        updateCacheTime()
        val claim = getClaim(player, nation) ?: return Influence(0)
        val reduceAmount = claim.reduce(amount, false)
        if (!claim.investedStrength.nonZero) {
            claims.remove(claim)
        }
        getTile().getMap().updateHighestLevelClaim()
        return reduceAmount
    }

    fun evict(claim: Claim) {
        assert(claim.claimData == this) { "Attempted to evict a claim from a different tile!" }
        updateCacheTime()
        claim.evict()
    }

    fun unevict(claim: Claim) {
        assert(claim.claimData == this) { "Attempted to unevict a claim from a different tile!" }
        updateCacheTime()
        claim.unevict()
    }

    fun transferToClaim(member: MemberWrapper, targetPlayer: Player, grantAmount: Influence) {
        assert(getClaim(member) != null) { "Could not find any claim belonging to the specified member!" }
        updateCacheTime()
        getClaim(member)!!.transferToClaim(targetPlayer, grantAmount)
    }

    /**
     * Gets the strength of the claim made by the given player.
     * Will return the 'none' influence if no claim was found.
     */
    private fun getUserStrength(userId: String?): Influence {
        for (claim in activeClaims) {
            if (claim.getUserId() == userId) {
                return claim.getStrength()
            }
        }
        return Influence.none()
    }

    //---CACHE STUFF

    /**
     * Returns the last time that a change was made to the claims on this TileClaimData instance.
     * This is used by cache functions to determine whether it should perform the (sometimes expensive)
     * recalculation of some value.
     */
    @Transient
    var lastModificationTime: LocalDateTime = LocalDateTime.now()

    fun updateCacheTime() {
        lastModificationTime = LocalDateTime.now()
    }

    //this means a player allegiance change will have to update every single one of these claims.
    /**
     * Unfortunately, this method cannot be cached. Whether a claim is active
     * depends not only on modifications to the claim state, but also on the properties
     * of users.
     * As such, calls to activeClaims should be made infrequently, and calls
     * from cache methods to other cache methods should also be avoided.
     */
    private val activeClaimsGetter: () -> List<Claim>
        get() = {
            val activeClaims: MutableList<Claim> = ArrayList()
            for (claim in claims) {
                if (claim.isActive) activeClaims.add(claim)
            }
            activeClaims
        }
    @Transient
    private var activeClaimsCache: Cache<List<Claim>> = Cache(activeClaimsGetter)
    private val activeClaims: List<Claim>
        get() {
            activeClaimsCache = activeClaimsCache ?: Cache(activeClaimsGetter)
            return activeClaimsCache.retrieve(lastModificationTime)
        }


    private val getTotalStrength: () -> Influence
        get() = {
            activeClaims.map { claim -> claim.strength }
                    .reduce { running, next -> running.add(next) }
        }
    @Transient
    private var totalStrengthCache: Cache<Influence> = Cache(getTotalStrength)
    private val totalStrength: Influence
        get() {
            totalStrengthCache = totalStrengthCache ?: Cache(getTotalStrength)
            return totalStrengthCache.retrieve(lastModificationTime)
        }

    /**
     * A lambda function that returns a map of nation to influence; if you
     * put in a given nation, you will get the sum of all active influence
     * working for that nation on behalf of this tile.
     * This is *only* to be used by other methods, as the internal claim strength
     * is never directly used. Instead, the effectiveClaimStrength of the owner
     * is used.
     */
    private val internalNationClaimStrengthsGetter: () -> HashMap<Nation, Influence>
        get() = {
            val nationalClaimStrengths = HashMap<Nation, Influence>()
            for (claim in activeClaims) {
                nationalClaimStrengths[claim.getNation()] = nationalClaimStrengths.getOrDefault(claim.getNation(), Influence(0)).add(claim.getStrength())
            }
            nationalClaimStrengths
        }
    @Transient
    private var internalNationStrengthsCache: Cache<HashMap<Nation, Influence>> = Cache(internalNationClaimStrengthsGetter)
    private val internalNationalStrengths: HashMap<Nation, Influence>
        get() {
            internalNationStrengthsCache = internalNationStrengthsCache ?: Cache(internalNationClaimStrengthsGetter)
            return internalNationStrengthsCache.retrieve(lastModificationTime)
        }
    private fun internalNationStrengthOf(nation: Nation): Influence {
        return internalNationalStrengths[nation] ?: Influence.none()
    }


    /**
     * Gets the nation whose members have the strongest combined claim on this tile.
     * @return The nation that owns this tile (or null if there are no national claims).
     */
    private val owningNationGetter: () -> Nation?
        get() = {
            internalNationalStrengths.maxByOrNull { entry ->
                entry.value.value
            }
                    ?.takeIf { it.value > totalStrength.applyModifier(0.5) } //over half
                    ?.key
        }
    @Transient
    private var owningNationCache: Cache<Nation?> = Cache(owningNationGetter)
    val owningNation: Nation?
        get() {
            owningNationCache = owningNationCache ?: Cache(owningNationGetter)
            return owningNationCache.retrieve(lastModificationTime)
        }

    private val owningNationName: String
        get() = tile.map.gameNetwork.guildWithAllegiance(owningNation)?.name ?: "Wildnerness"


    private val effectiveNationClaimStrengthGetter: () -> Influence
        get() = {
            internalNationalStrengths[owningNation]
                    ?.let { it.subtract(totalStrength.subtract(it)) } //reduce strength
                    ?.takeUnless { it.isNegative } //don't take non-majority national strengths
                    ?: Influence.none()
        }
    @Transient
    private var effectiveNationClaimStrengthCache: Cache<Influence> = Cache(effectiveNationClaimStrengthGetter)
    val effectiveNationalStrength: Influence
        get() {
            effectiveNationClaimStrengthCache = effectiveNationClaimStrengthCache ?: Cache(effectiveNationClaimStrengthGetter)
            return effectiveNationClaimStrengthCache.retrieve(lastModificationTime)
        }


    /**
     * Gets the user who has the strongest relevant claim on this tile.
     * @return The user who owns this tile (or null if there are no active claims).
     */
    private val owningPlayerGetter: () -> Player?
        get() = {
            activeClaims
                    .filter { claim -> claim.nation == owningNation }
                    .maxByOrNull { claim -> claim.strength.value }
                    ?.takeIf { it.strength > effectiveNationalStrength.applyModifier(0.5) } //over half
                    ?.player
        }
    @Transient
    private var owningPlayerCache: Cache<Player?> = Cache(owningPlayerGetter)
    val owningPlayer: Player?
        get() {
            owningPlayerCache = owningPlayerCache ?: Cache(owningPlayerGetter)
            return owningPlayerCache.retrieve(lastModificationTime)
        }


    private val ownerStrengthGetter: () -> Influence
        get() = {
            activeClaims
                    .firstOrNull { claim -> claim.player == owningPlayer && claim.nation == owningNation }
                    ?.strength
                    ?.let { it.subtract(effectiveNationalStrength.subtract(it)) }
                    ?.let { if (effectiveNationalStrength < it) effectiveNationalStrength else it } //cap at national strength
                    ?.takeUnless { it.isNegative }
                    ?: Influence.none()
        }
    @Transient
    private var ownerStrengthCache: Cache<Influence> = Cache(ownerStrengthGetter)
    val ownerStrength: Influence
        get() {
            ownerStrengthCache = ownerStrengthCache ?: Cache(ownerStrengthGetter)
            return ownerStrengthCache.retrieve(lastModificationTime)
        }

    private val claimLevelGetter: () -> Double
    get() = {
        Utilities.triangulateWithRemainder(ownerStrength.value)
                .run {
                    println("Level on this tile: $this (from owner strength $ownerStrength)")
                    this
                }
    }
    @Transient
    private var claimLevelCache: Cache<Double> = Cache(claimLevelGetter)
    val claimLevel: Double
        get() {
            claimLevelCache = claimLevelCache ?: Cache(claimLevelGetter)
            return claimLevelCache.retrieve(lastModificationTime)
        }

    override fun adopt() {
        updateCacheTime()
        for (claim in claims) {
            claim.setParent(this)
        }
    }

    val claimList: String
        get() {
            val lines: MutableList<String> = ArrayList()
            val owningNation = owningNation
            if (owningNation != null) {
                for (nation in tile.map.gameNetwork.nations) {
                    val strength = internalNationStrengthOf(nation)
                    if (!strength.nonZero) continue
                    var effectiveString: String
                    effectiveString = if (tile.map.gameNetwork.isTutorial()) {
                        nation.toString() + ": " + String.format("%s", strength)
                    } else {
                        nation.effectiveName + ": " + String.format("%s", strength)
                    }
                    if (owningNation === nation) {
                        lines.add("**$effectiveString**")
                    } else {
                        lines.add(effectiveString)
                    }
                }
            }
            return java.lang.String.join("\n", lines)
        }

    @JvmOverloads
    fun displayClaims(format: Format, amount: Int = 3, asker: Player? = null): String {
        val askerClaim: Claim?
        askerClaim = if (asker != null) {
            getClaim(asker, asker.allegiance)
        } else {
            null
        }
        val tinyFormat = owningNation?.let {String.format("%s/%s/%s",
                internalNationStrengthOf(it),
                getUserStrength(owningPlayer?.userWrapper?.id),
                totalStrength.toString())} ?: "Wildnerness"
        return if (format == Format.TINY) {
            tinyFormat
        } else if (format == Format.ONE_LINE) {
            val ownerName = owningPlayer?.name
            if (owningPlayer == null) {
                String.format("%s of %s (%s)",
                        ownerName,
                        owningNation!!.effectiveName,
                        tinyFormat)
            } else {
                "Wildnerness"
            }
        } else if (format == Format.SIMPLE) {
            if (owningPlayer == null) {
                return "Wildnerness"
            }
            var nationalCompetitionByColor = ""
            val nationalCompetitors: MutableList<String> = ArrayList()
            for (nation in tile.map.gameNetwork.nations) {
                if (nation === owningNation) continue
                if (internalNationStrengthOf(nation).thousandths > 0) {
                    nationalCompetitors.add(String.format("%s: %s",
                            nation,
                            internalNationStrengthOf(nation)))
                }
            }
            if (!nationalCompetitors.isEmpty()) {
                nationalCompetitionByColor = "(" + java.lang.String.join(", ", nationalCompetitors) + ")"
            }
            var youTag = ""
            if (askerClaim != null && askerClaim.investedStrength.thousandths > 0) {
                youTag = String.format("*(%s)*", askerClaim.toString())
            }
            String.format("""
    **%s: %s** %s
    %s: %s %s
    """.trimIndent(),
                    owningNationName,
                    effectiveNationalStrength,
                    nationalCompetitionByColor,
                    owningPlayer?.name,
                    ownerStrength,
                    youTag)
        } else if (format == Format.COMPETITIVE) {
            if (owningPlayer == null) {
                return "Wildnerness"
            }
            var nationalCompetition = ""
            val nationalCompetitors: MutableList<String> = ArrayList()
            for (nation in tile.map.gameNetwork.nations) {
                if (nation == owningNation) continue
                if (internalNationStrengthOf(nation).thousandths > 0.0) {
                    nationalCompetitors.add(String.format("%s: %s",
                            nation.effectiveName,
                            internalNationStrengthOf(nation)))
                }
            }
            if (nationalCompetitors.isNotEmpty()) {
                nationalCompetition = "(" + java.lang.String.join(", ", nationalCompetitors) + ")"
            }
            val nationalState = String.format("**%s: %s** %s\n",
                    owningNationName,
                    effectiveNationalStrength,
                    nationalCompetition)
            var claims = this.claims.toMutableList()
            claims.sortByDescending{ x: Claim -> x.getStrength().thousandths }
            if (claims.size > amount) {
                claims = claims.subList(0, amount)
            }
            if (askerClaim != null && !claims.contains(askerClaim) && askerClaim.isActive && askerClaim.getNation() == owningNation) {
                getClaim(asker, asker?.allegiance)?.let { claims.add(it) }
            }
            val claimDisplays = claims.stream().map { x: Claim? -> x.toString() }.collect(Collectors.toList())
            nationalState + String.format(java.lang.String.join("\n", claimDisplays))
        } else {
            claimList
        }
    }

    enum class Format(var format: String) {
        //1.5/1.8/2 //last value hidden for private servers
        TINY("%o:a%/%n:a%/%nt:a%"),  //Hyreon (1.5/1.8) of Hyreon's Domain (1.8/3) //last tag hidden for private servers
        ONE_LINE("%o% (%o:a%/%n:a%) of %n% (%n:a%/%nt:a%)"),  /* Can be modified by a number of nations to view
        **Hyreon's Domain: 1.8** (RED: 1.1, GREEN: 0.1) //tut/main
        **Total: 1.8** //private servers
        Hyreon: 1.5 (You: 0.02) //'you' is not there if 0
         */
        SIMPLE("""
    %n%: %n:a% (%n2:c%: %n2:a%, %n3:c%: %n3:a%)
    %o%: %o:a%[y: (%y%: %y:a%)]
    """.trimIndent()),  /* Can be modified by a number of players to view
        **Hyreon's Domain: 1.8** (duck home: 1.1, Green Server: 0.1) //tut/main
        **Total: 1.8** //private servers
        **1. Hyreon: 1.5** //1st (in the case of ties, both display as 1)
        2. Metel: 0.1 //2nd
        3. Terandr: 0.1 //3rd
        5. *You: 0.02* //you, or nothing if in top 3 / is 0
         */
        COMPETITIVE("""
    %n%: %n:a% (%n2%: %n2:a%, %n3%: %n3:a%)
    %o%: %o:a%[p2:
    %p2%: %p2:a%][p3:
    (%p3%: %p3:a%)][y*:
    *%y%: %y:a%*]
    """.trimIndent()),  /* Can be modified by a number of players to view
        **Hyreon's Domain: 1.8** (duck home: 1.1, Green Server: 0.1) //tut/main
        **Total: 1.8** //private servers
        **1. Hyreon: 1.5** //1st (in the case of ties, both display as 1)
        - Rayzr522: 0.9
        - MC_2018: 0.2
        2. Metel: 0.1 //2nd
        3. Terandr: 0.1 //3rd
        5. *You: 0.02* //you, or nothing if in top 3 / is 0
         */
        EXTENDED(""),  /*
        Competitive, but it shows all claims, even invalid ones
         */
        COMPREHENSIVE("");
    }
}