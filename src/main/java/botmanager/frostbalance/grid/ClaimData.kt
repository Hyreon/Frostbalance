package botmanager.frostbalance.grid

import botmanager.Utils
import botmanager.frostbalance.Influence
import botmanager.frostbalance.Nation
import java.util.*
import java.util.stream.Collectors

class ClaimData(tile: Tile?) : TileData(tile), Container {

    private val owningNationName: String
        get() {
            return tile.map.gameNetwork.guildWithAllegiance(owningNation)?.name ?: "Wildnerness"
        }

    /**
     * The strength of each players' claim to a specific tile. See Claim for details.
     */
    var claims: MutableList<Claim> = ArrayList()
        get() = field

    private var lastOwningNation: Nation? = null
    private var lastOwningUserId: String? = null
    override fun getTile(): Tile {
        return tile
    }

    /**
     * Gets the user who has the strongest relevant claim on this tile.
     * @return The user who owns this tile (or null if there are no active claims).
     */
    val owningUserId: String?
        get() {
            if (claims.isEmpty()) return null
            owningNation
            var selectedUserId: String? = null
            var selectedStrength = Influence(0)
            for (claim in activeClaims) {
                if (claim.getNation() !== lastOwningNation) continue
                if (!claim.isActive) continue
                if (claim.getStrength() > selectedStrength ||
                        lastOwningUserId == claim.getUserId() && claim.getStrength() == selectedStrength) {
                    selectedUserId = claim.getUserId()
                    selectedStrength = claim.getStrength()
                }
            }
            lastOwningUserId = selectedUserId
            return selectedUserId
        }

    /**
     * Gets the nation whose members have the strongest combined claim on this tile.
     * Note that this will return NONE every time for world maps outside the global set.
     * @return The nation that owns this tile (or NONE if there are no national claims).
     */
    val owningNation: Nation?
        get() {
            if (claims.isEmpty()) return null
            var selectedNation = lastOwningNation
            var selectedStrength = lastOwningNation?.let { getNationalStrength(it) } ?: Influence.none()
            for (nation in tile.map.gameNetwork.nations) {
                val nationalStrength = getNationalStrength(nation)
                if (nationalStrength > selectedStrength) {
                    selectedNation = nation
                    selectedStrength = nationalStrength
                }
            }
            if (selectedStrength.thousandths <= 0) {
                return null
            }
            lastOwningNation = selectedNation
            return selectedNation
        }

    /**
     * Adds a claim with the given parameters to this tile.
     * @param player
     * @param nation
     * @param strength
     */
    fun addClaim(player: PlayerCharacter?, nation: Nation?, strength: Influence?): Claim {
        for (claim in claims) {
            if (claim.overlaps(this, player, nation)) {
                claim.add(strength)
                owningNation
                getTile().getMap().updateStrongestClaim()
                return claim
            }
        }
        val newClaim = Claim(player, nation, strength)
        owningNation
        getTile().getMap().updateStrongestClaim()
        return newClaim
    }

    fun addClaim(player: PlayerCharacter, strength: Influence?): Claim {
        return addClaim(player, player.nation, strength)
    }

    fun addClaim(newClaim: Claim): Claim {
        for (claim in claims) {
            require(!newClaim.overlaps(claim)) { "This claim overlaps an existing claim!" }
        }
        claims.add(newClaim)
        owningNation
        getTile().getMap().updateStrongestClaim()
        return newClaim
    }

    fun getClaim(player: PlayerCharacter?, nation: Nation?): Claim? {
        for (claim in claims) {
            if (claim.overlaps(this, player, nation)) {
                return claim
            }
        }
        return null
    }

    /**
     * Reduce the strength of a claim.
     * Any player can do this to their own claims at no cost, but with no refund.
     * @return
     */
    fun reduceClaim(player: PlayerCharacter?, nation: Nation?, amount: Influence?): Influence {
        val claim = getClaim(player, nation) ?: return Influence(0)
        getTile().getMap().updateStrongestClaim()
        return claim.reduce(amount)
    }

    private fun getUserStrength(userId: String?): Influence {
        for (claim in activeClaims) {
            if (claim.getUserId() == userId) {
                return claim.getStrength()
            }
        }
        return Influence(0)
    }

    fun getNationalStrength(nation: Nation): Influence {
        var nationalStrength = Influence.none()
        for (claim in activeClaims) {
            if (claim.getNation() === nation) {
                nationalStrength = nationalStrength.add(claim!!.getStrength())
            }
        }
        return nationalStrength
    }

    val nationalStrength: Influence
        get() {
            val nationalClaimStrengths = HashMap<Nation, Influence>()
            for (claim in activeClaims) {
                nationalClaimStrengths[claim.getNation()] = nationalClaimStrengths.getOrDefault(claim.getNation(), Influence(0)).add(claim.getStrength())
            }
            var selectedStrength = Influence(0)
            for (nation in nationalClaimStrengths.keys) {
                if (nationalClaimStrengths.getOrDefault(nation, Influence.none()) > selectedStrength) {
                    selectedStrength = nationalClaimStrengths.getOrDefault(nation, Influence.none())
                }
            }
            return selectedStrength
        }

    val nationalDominance: Influence
        get() {
            return nationalStrength.subtract(
                    tile.map.gameNetwork.nations
                            .filter { nation -> nation != owningNation }
                            .minByOrNull { nation -> getNationalStrength(nation).value }
                            ?.let {getNationalStrength(it)}
                            ?: Influence.none()
            )
        }

    val activeClaims: MutableList<Claim>
        get() {
            val activeClaims: MutableList<Claim> = ArrayList()
            for (claim in claims) {
                if (claim.isActive) activeClaims.add(claim)
            }
            return activeClaims
        }

    override fun adopt() {
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
                    val strength = getNationalStrength(nation)
                    if (strength.thousandths == 0) continue
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
    fun displayClaims(format: Format, amount: Int = 3, asker: PlayerCharacter? = null): String {
        val askerClaim: Claim?
        askerClaim = if (asker != null) {
            getClaim(asker, asker.nation)
        } else {
            null
        }
        val tinyFormat = owningNation?.let {String.format("%s/%s/%s",
                getNationalStrength(it),
                getUserStrength(owningUserId),
                totalStrength.toString())} ?: "Wildnerness"
        return if (format == Format.TINY) {
            tinyFormat
        } else if (format == Format.ONE_LINE) {
            val ownerName = owningUserName
            if (!Utils.isNullOrEmpty(owningUserId)) {
                String.format("%s of %s (%s)",
                        ownerName,
                        owningNation!!.effectiveName,
                        tinyFormat)
            } else {
                "Wildnerness"
            }
        } else if (format == Format.SIMPLE) {
            if (Utils.isNullOrEmpty(owningUserId)) {
                return "Wildnerness"
            }
            var nationalCompetitionByColor = ""
            val nationalCompetitors: MutableList<String> = ArrayList()
            for (nation in tile.map.gameNetwork.nations) {
                if (nation === owningNation) continue
                if (getNationalStrength(nation).thousandths > 0) {
                    nationalCompetitors.add(String.format("%s: %s",
                            nation,
                            getNationalStrength(nation)))
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
                    nationalStrength,
                    nationalCompetitionByColor,
                    owningUserName,
                    ownerStrength,
                    youTag)
        } else if (format == Format.COMPETITIVE) {
            if (Utils.isNullOrEmpty(owningUserId)) {
                return "Wildnerness"
            }
            var nationalCompetition = ""
            val nationalCompetitors: MutableList<String> = ArrayList()
            for (nation in tile.map.gameNetwork.nations) {
                if (nation === owningNation) continue
                if (getNationalStrength(nation).thousandths > 0.0) {
                    nationalCompetitors.add(String.format("%s: %s",
                            nation.effectiveName,
                            getNationalStrength(nation)))
                }
            }
            if (nationalCompetitors.isNotEmpty()) {
                nationalCompetition = "(" + java.lang.String.join(", ", nationalCompetitors) + ")"
            }
            val nationalState = String.format("**%s: %s** %s\n",
                    owningNationName,
                    nationalStrength,
                    nationalCompetition)
            var claims = this.claims.toMutableList()
            claims.sortByDescending{ x: Claim -> x.getStrength().thousandths }
            if (claims.size > amount) {
                claims = claims.subList(0, amount)
            }
            if (askerClaim != null && !claims.contains(askerClaim) && askerClaim.isActive && askerClaim.getNation() == owningNation) {
                getClaim(asker, asker?.nation)?.let { claims.add(it) }
            }
            val claimDisplays = claims.stream().map { x: Claim? -> x.toString() }.collect(Collectors.toList())
            nationalState + String.format(java.lang.String.join("\n", claimDisplays))
        } else {
            claimList
        }
    }

    private val ownerStrength: Influence
        get() = getUserStrength(owningUserId)
    private val owningUser: PlayerCharacter?
        get() = PlayerCharacter.get(owningUserId, getTile().getMap())
    private val owningUserName: String
        get() = if (owningUser != null) {
            owningUser!!.name
        } else {
            "Nobody"
        }
    private val totalStrength: Influence
        get() {
            var totalStrength = Influence(0)
            for (claim in activeClaims) {
                if (claim!!.getStrength().thousandths > 0) totalStrength = totalStrength.add(claim.getStrength())
            }
            return totalStrength
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