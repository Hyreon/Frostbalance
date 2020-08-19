package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.command.CommandContext
import net.dv8tion.jda.api.EmbedBuilder
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.min

abstract class ListMenu<T>(bot: Frostbalance, context: CommandContext, private val items: List<T>) : Menu(bot, context) {

    val PAGE_SIZE = 7

    var page = 1
    var pageSize = PAGE_SIZE

    val sublist: List<T>
        get() = items.subList((page - 1) * pageSize, min(page * pageSize, items.size))

    init {

        menuResponses.add(object : MenuResponse("⬅", "Previous") {
            override fun reactEvent() {
                previousPage()
            }

            override fun isValid(): Boolean {
                return page > 1
            }
        })
        menuResponses.add(object : MenuResponse("➡", "Next") {
            override fun reactEvent() {
                nextPage()
            }

            override fun isValid(): Boolean {
                return page < maxPages()
            }
        })
        menuResponses.add(object : MenuResponse("✅", "Exit") {
            override fun reactEvent() {
                closeAll(false)
            }

            override fun isValid(): Boolean {
                return true
            }
        })

    }

    constructor(bot: Frostbalance, context: CommandContext, options: List<T>, page: Int) : this(bot, context, options) {
        this.page = page
    }

    fun previousPage() {
        page--
        updateMessage()
    }

    fun nextPage() {
        page++
        updateMessage()
    }

    fun maxPages(): Int {
        return ceil(items.size / pageSize.toDouble()).toInt()
    }

    override val embedBuilder: EmbedBuilder
        get() = EmbedBuilder()
        .setTitle("Page $page/${maxPages()}")
        .setDescription(displayList())

    private fun displayList(): String? {
        val displayList = sublist.stream().map { obj: T -> obj.toString() }.collect(Collectors.toList())
        return java.lang.String.join("\n", displayList)
    }
}