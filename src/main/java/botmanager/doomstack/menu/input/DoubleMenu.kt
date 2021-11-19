package botmanager.doomstack.menu.input

import botmanager.doomstack.DoomStack
import botmanager.doomstack.MessageContext
import botmanager.doomstack.menu.option.OptionMenu
import botmanager.doomstack.menu.response.NumberHook
import net.dv8tion.jda.api.EmbedBuilder

class DoubleMenu(bot: DoomStack, context: MessageContext,
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
            return super.embedBuilder
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