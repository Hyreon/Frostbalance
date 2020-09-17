package botmanager.frostbalance.menu.input

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.option.OptionMenu
import botmanager.frostbalance.menu.response.NumberHook
import net.dv8tion.jda.api.EmbedBuilder

class DoubleMenu(bot: Frostbalance, context: MessageContext,
                 private val operation: (value: Double) -> Unit,
                 private val filter: (value: Double) -> Boolean = { true },
                 values: List<Double> = emptyList(),
                 private val title: String,
                 private val description: String =
                          "Choose a value or type it out below.")
    : OptionMenu<Double>(bot, context, values) {

    init {
        hook(object : NumberHook(this, "*Or, type out the multiplier below...*") {

            override fun hookEvent(hookContext: MessageContext) {
                select(hookContext.message.contentRaw.toDouble())
            }

        })
    }

    override val embedBuilder: EmbedBuilder
        get() {
            return EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
        }

    override fun select(option: Double) {
        if (filter(option)) {
            operation(option)
            close(false)
        } else {
            context.sendResponse("That number can't be used, try again.")
            updateMessage()
        }
    }

}