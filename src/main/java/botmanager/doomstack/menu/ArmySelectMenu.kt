package botmanager.doomstack.menu

import botmanager.doomstack.CombatUnit
import botmanager.doomstack.CombatUnitClass.Companion.ARCHER
import botmanager.doomstack.CombatUnitClass.Companion.HORSEMAN
import botmanager.doomstack.CombatUnitClass.Companion.SPEARMAN
import botmanager.doomstack.CombatUnitClass.Companion.WARRIOR
import botmanager.doomstack.CombatUnitClass.Companion.ancientPool
import botmanager.doomstack.CombatUnitClass.Companion.medievalPool
import botmanager.doomstack.CombatUnitClass.Companion.totalPool
import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.UnitStack
import botmanager.doomstack.menu.option.OptionMenu
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import java.awt.Color

class ArmySelectMenu(bot: DoomStack, context: MessageContext, var challenger: User, private val stakes: Double = 200.0) : OptionMenu<UnitStack>(bot, context, listOf(
    UnitStack(type = ancientPool.random(), rank =  CombatUnit.Rank.values().random(), budget = stakes),
    UnitStack(type = medievalPool.random(), rank =  CombatUnit.Rank.values().random(), budget = stakes),
    UnitStack(type = totalPool.random(), rank =  CombatUnit.Rank.values().random(), budget = stakes)
)
    ) {

    var defenders: UnitStack? = null

    override fun select(option: UnitStack) {
        if (defenders == null) {
            defenders = option
            originalMenu.actor = challenger.also { challenger = originalMenu.actor!! }
            updateMessage()
        } else {
            originalMenu.actor = challenger.also { challenger = originalMenu.actor!! }
            val fightMenu = FightMenu(bot, context, option, defenders!!)
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
                if (defenders == null) {
                    builder.setDescription("*${originalMenu.actor!!.asMention}, pick your defense!*")
                } else {
                    builder.setDescription("*${originalMenu.actor!!.asMention}, pick your doom stack!*")
                }
            }
            return builder
        }

}