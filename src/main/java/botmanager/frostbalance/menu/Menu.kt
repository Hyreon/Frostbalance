package botmanager.frostbalance.menu

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.UserWrapper
import botmanager.frostbalance.command.MessageContext
import botmanager.frostbalance.menu.response.MenuResponse
import botmanager.frostbalance.menu.response.MenuTextHook
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote
import net.dv8tion.jda.api.requests.RestAction
import java.io.File
import java.util.*

//TODO split into parent and child; don't force childs to store message data, and use a val instead for them
abstract class Menu(protected var bot: Frostbalance, val context : MessageContext) {

    //TODO don't use message caches directly, they become dated. This goes for CommandContext as well!
    //TODO try for better polymorphism with CommandContext and GuildCommandContext, forcing GuildCommandContext where desired.

    /**
     * The message this menu is attached to. This can be null if the menu hasn't yet been sent.
     */
    var message: Message? = null
    var actor: UserWrapper? = null

    var isClosed = false
    @JvmField
    var menuResponses: MutableList<MenuResponse> = ArrayList()

    var hook: MenuTextHook? = null
        get() = if (this != activeMenu) activeMenu.hook else field

    val hasHook: Boolean
        get() = hook != null

    protected fun hook(hook: MenuTextHook) {
        this.hook = hook
    }

    open fun send() {
        return send(context.channel, context.author)
    }

    open fun send(channel: MessageChannel, actor: UserWrapper) {
        bot.addMenu(this)
        this.actor = actor
        val me = messageEmbed
        message = if (me.image != null && me.image!!.url!!.contains("attachment://")) {
            val fileName = me.image!!.url!!.replace("attachment://", "")
            channel.sendFile(File(fileName)).embed(me).complete()
        } else {
            channel.sendMessage(me).complete()
        }
        smartUpdateEmojis()
    }

    open fun updateMessage() {
        val me = activeMenu.messageEmbed
        editAndSetMessage(me)
        smartUpdateEmojis()
    }

    private fun editAndSetMessage(me: MessageEmbed) {
        if (me.image != null &&
            me.image!!.url != null &&
            me.image!!.url!!.contains("attachment://")) {
            val fileName = me.image!!.url!!.replace("attachment://", "")
            originalMenu.message!!.delete().queue()
            originalMenu.message = originalMenu.message!!.channel.sendFile(File(fileName)).embed(me).complete()
        } else {
            originalMenu.message = originalMenu.message!!.editMessage(me).complete()
        }
    }

    private fun smartUpdateEmojis() {
        if (originalMenu.message == null) return
        if (smartUpdateCost > 0) return rewriteEmojis()
        if (activeMenu.context.isPublic) {
            if (!isClosed) {
                val requestInfo = clearIncorrectReactionRequest()
                var clearEvent = requestInfo.first
                val startIndex = requestInfo.second
                activeMenu.menuResponses.subList(startIndex, activeMenu.menuResponses.size).forEach { menuResponse ->
                    if (menuResponse.isValid) {
                        //TODO don't try to add reactions if the message has been deleted.
                        val addReactEvent = originalMenu.message!!.addReaction(menuResponse.emoji)
                        clearEvent = clearEvent?.flatMap { println("Adding ${menuResponse.emoji.encodeToByteArray()}"); addReactEvent }
                                ?: run{ println("Brand new add event"); addReactEvent }
                    }
                }
                clearEvent?.queue()
            } else {
                message?.clearReactions()?.queue()
            }
        } else {
            if (isClosed) {
                for (menuResponse in activeMenu.menuResponses) {
                    originalMenu.message!!.removeReaction(menuResponse.emoji).queue()
                }
            } else if (!message!!.isEdited) {
                for (menuResponse in activeMenu.menuResponses) {
                    //TODO don't try to add reactions if the message has been deleted.
                    originalMenu.message!!.addReaction(menuResponse.emoji).queue()
                }
            }
        }
    }

    private fun rewriteEmojis() {
        if (message == null) return
        if (activeMenu.context.isPublic) {
            message!!.clearReactions().queue()
            if (!isClosed) {
                for (menuResponse in activeMenu.menuResponses) {
                    if (menuResponse.isValid) {
                        //TODO don't try to add reactions if the message has been deleted.
                        originalMenu.message!!.addReaction(menuResponse.emoji).queue()
                    }
                }
            }
        } else {
            if (isClosed) {
                for (menuResponse in activeMenu.menuResponses) {
                    originalMenu.message!!.removeReaction(menuResponse.emoji).queue()
                }
            } else if (!message!!.isEdited) {
                for (menuResponse in activeMenu.menuResponses) {
                    //TODO don't try to add reactions if the message has been deleted.
                    originalMenu.message!!.addReaction(menuResponse.emoji).queue()
                }
            }
        }
    }

    @kotlin.Deprecated("")
    val jdaActor: User? //may be null if the user is now inaccessible
        get() = actor?.jdaUser


    //TODO find the time taken to do a smart update, and compare it to the time taken for a dumb update
    private val smartUpdateCost: Int
        get() = {
            0
        }.invoke()

    /**
     * Returns: the action that would clear all incorrect reactions,
     * and the number of correct reactions left after this.
     */
    private fun clearIncorrectReactionRequest(): Pair<RestAction<Void>?, Int> {
        var clearEvent: RestAction<Void>? = null
        originalMenu.message?.reactions
                ?.filter { reaction -> !reaction.reactionEmote.isEmoji }
                ?.forEach { reaction -> clearEvent = reaction.clearInstanceEvent(clearEvent) }
        var index = 0
        originalMenu.message?.reactions?.forEach { existingReaction ->
            if (activeMenu.menuResponses
                            .filter { response -> response.isValid }
                            .getOrNull(index)
                            ?.let { it.emoji != existingReaction.reactionEmote.emoji } != false) { //skip if it's what it should be
                clearEvent = existingReaction.clearInstanceEvent(clearEvent)
            } else {
                clearEvent = existingReaction.clearInstanceEvent(clearEvent, false)
                index += 1 //save the first reaction and do nothing to it
            }
        }
        return Pair(clearEvent, index)
    }

    fun close(delete: Boolean) {
        isClosed = true
        bot.removeMenu(this)
        if (child != null) {
            child!!.close(delete)
        }
        if (parent != null) {
            parent!!.disown()
        } else {
            if (delete) {
                message?.delete()?.queue()
            } else {
                updateMessage()
            }
        }
    }

    fun closeAll(delete: Boolean) {
        originalMenu.close(delete)
    }

    /**
     * Uses the defined MEBuilder and adds the emoji options this menu provides.
     * @return The MessageEmbed this menu is supposed to provide
     */
    val messageEmbed: MessageEmbed
        get() {
            val embedBuilder = embedBuilder
            embedBuilder.updateContextDisplay()
            if (!isClosed) {
                var description = ""
                for (menuResponse in activeMenu.menuResponses) {
                    if (menuResponse.isValid) {
                        description += "${menuResponse.emoji} ${menuResponse.name}\n"
                    }
                }
                if (hasHook) {
                    description += "⌨️ *${hook!!.name}*\n"
                }
                embedBuilder.addField("Options", description, false)
            }
            return embedBuilder.build()
        }

    abstract val embedBuilder: EmbedBuilder

    fun applyResponse(reactionEmote: ReactionEmote) {
        for (menuResponse in activeMenu.menuResponses) {
            if (reactionEmote.emoji == menuResponse.emoji) {
                menuResponse.applyReaction()
            }
        }
    }
    /*
     EmbedBuilder embedBuilder = new EmbedBuilder();
     embedBuilder.setColor(Color.RED);
     embedBuilder.setTitle("Title");
     embedBuilder.setAuthor("Author");
     embedBuilder.setDescription("Description");
     embedBuilder.setFooter("Footer");
     embedBuilder.setImage("https://mobileimages.lowes.com/product/converted/693092/693092000005xl.jpg");
     embedBuilder.setThumbnail("https://images.homedepot-static.com/productImages/21bd11f8-81e9-4ea6-a9c8-cba1ed8119e7/svn/bricks-red0126mco-64_300.jpg");
     embedBuilder.addField("", "Value1", false);
     return embedBuilder.build();
     */

    var parent: Menu? = null
    var child: Menu? = null

    /**
     * Forces a menu to render a menu other than itself.
     * This can be done ad infinitum, and either menu can
     * still be closed; closing any menu will also close the
     * menus it is told to render.
     */
    fun redirectTo(menu: Menu, cancelable : Boolean) {
        if (child != null) {
            disown()
        }
        child = menu
        child!!.parent = this
        if (cancelable) {
            child!!.menuResponses.add(object : MenuResponse("⤴", "Previous Menu") {
                override fun reactEvent() {
                    child!!.close(false)
                }

                override val isValid: Boolean
                    get() = true
            })
        }
        updateMessage()
    }

    private fun disown() {
        if (child != null) {
            child?.disown()
            child?.parent = null
            child = null
            updateMessage()
        }
    }

    val originalMenu: Menu
        get() {
            var mostGeezerishMenu: Menu = this
            while (mostGeezerishMenu.parent != null) {
                mostGeezerishMenu = mostGeezerishMenu.parent!!
            }
            return mostGeezerishMenu
        }

    val activeMenu: Menu
        get() {
            var mostBabyishMenu: Menu = this
            while (mostBabyishMenu.child != null) {
                mostBabyishMenu = mostBabyishMenu.child!!
            }
            return mostBabyishMenu
        }

    private fun MessageReaction.clearInstanceEvent(event: RestAction<Void>?, self: Boolean = true): RestAction<Void>? {
        var returnEvent = event
        val users = retrieveUsers().complete()
        users.forEach { user ->
            if (originalMenu.message!!.isFromGuild && (self || bot.jda.selfUser != user)) {
                val newEvent = removeReaction(user)
                returnEvent = returnEvent?.flatMap{ println("Deleting ${this.reactionEmote.emoji} from $user"); newEvent } ?: run {
                    println("Brand new delete event"); newEvent
                }
            }
        }
        return returnEvent
    }

    fun isChild(): Boolean {
        return child == null
    }

    /**
     * This updates the color and footer of an embed.
     */
    private fun EmbedBuilder.updateContextDisplay() {
        if (isClosed) {
            setColor(null)
        } else {
            setColor(context.guild?.color)
        }
        if (!context.isPublic) {
            setFooter(context.guild?.commandContextFooter())
        }
    }
}
