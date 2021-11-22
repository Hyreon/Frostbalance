package botmanager.doomstack.menu

import botmanager.doomstack.CombatUnit
import botmanager.doomstack.CombatUnitClass.Companion.ancientPool
import botmanager.doomstack.CombatUnitClass.Companion.industrialPool
import botmanager.doomstack.CombatUnitClass.Companion.medievalPool
import botmanager.doomstack.CombatUnitClass.Companion.modernPool
import botmanager.doomstack.CombatUnitClass.Companion.uniquePool
import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.UnitStack
import botmanager.doomstack.menu.option.OptionMenu
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import kotlin.random.Random

class ArmySelectMenu(bot: DoomStack, context: MessageContext, var challenger: User, stakes: Double = 200.0) : OptionMenu<List<UnitStack>>(bot, context, generate(stakes)
    ) {

    var defenders: List<UnitStack> = listOf()

    override fun select(option: List<UnitStack>) {
        if (defenders.isEmpty()) {
            defenders = option
            originalMenu.actor = challenger.also { challenger = originalMenu.actor!! }
            updateMessage()
        } else {
            originalMenu.actor = challenger.also { challenger = originalMenu.actor!! }
            val fightMenu = FightMenu(bot, context, option, defenders)
            parent?.redirectTo(fightMenu, false)
            setDelegating(true)
            fightMenu.doTurn()
        }
    }

    override val embedBuilder: EmbedBuilder
        get() {
            val builder = super.embedBuilder
            if (isClosed) {
                builder.setColor(Color.DARK_GRAY)
                builder.setTitle(context.guild!!.getMember(challenger)!!.effectiveName + ": Win by default")
            } else {
                builder.setTitle(
                    context.guild!!.getMember(challenger)!!.effectiveName + " vs " + context.guild!!.getMember(originalMenu.actor!!)!!.effectiveName + "!"
                )
                if (defenders.isEmpty()) {
                    builder.setDescription("*${originalMenu.actor!!.asMention}, pick your defense!*")
                } else {
                    builder.setDescription("*${originalMenu.actor!!.asMention}, pick your doom stack!*")
                }
            }
            return builder
        }

}

fun generate(resources: Double): MutableList<List<UnitStack>> {
    val pools = listOf(ancientPool, ancientPool, ancientPool.union(medievalPool), uniquePool)
    val mutableList: MutableList<List<UnitStack>> = mutableListOf()
    pools.forEach { pool ->
        var resourcesRemaining = resources
        val combatants: MutableList<UnitStack> = mutableListOf()
        var i = 1
        do {
            val combatant = UnitStack(type = pool.random(), rank = CombatUnit.Rank.values().random(), budget = Random.nextDouble(resourcesRemaining))
            if (combatant.count > 0) {
                combatants.add(combatant)
                resourcesRemaining -= combatant.count * combatant.type.cost
            } else {
                continue
            }
            i++
        } while (i == 1 || (Random.nextDouble() < (resourcesRemaining / resources) && i < 3 && resourcesRemaining > 0))
        mutableList.add(combatants)
    }
    return mutableList
}
