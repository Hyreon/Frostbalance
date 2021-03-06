package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.input.ConfirmationMenu

class GrantClaimCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("grant"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val grantAmount = argumentStream.nextInfluence()
                ?: return context.sendResponse("The amount '${argumentStream.lastArgument}' wasn't recognized as a valid influence amount.")
        val targetPlayer = bot.getUserByName(argumentStream.exhaust(), context.guild)?.playerIn(context.gameNetwork)
                ?: return context.sendResponse("Could not find player '${argumentStream.lastArgument}'.")

        ConfirmationMenu(bot, context, {

            val influenceOnTile = claimData.getClaim(context.player, context.guild.nation)?.strength
                    ?: Influence.none()

            if (claimData.tile.location != context.player.character.location && influenceOnTile < grantAmount) {
                return@ConfirmationMenu context.sendResponse("Your $influenceOnTile influence on this tile isn't enough to grant $grantAmount. You need to walk to this tile to grant more.")
            }

            if (influenceOnTile + context.member.influence < grantAmount) {
                context.sendPrivateResponse("You don't have enough influence to grant this tile! Attempting to do so will spend **ALL** of your influence, and everyone will know you did this!")
            }

            ConfirmationMenu(bot, context, {

                val effectiveGrantAmount: Influence = if (influenceOnTile < grantAmount) {
                    val intendedChange = grantAmount.subtract(influenceOnTile)
                    val amountToChange = intendedChange.subtract(context.member.adjustInfluence(intendedChange.negate()))
                    claimData.addClaim(context.member, amountToChange)
                    amountToChange
                } else {
                    grantAmount
                }

                claimData.transferToClaim(context.member, targetPlayer, effectiveGrantAmount)
                context.sendResponse("You have given ${targetPlayer.name} $effectiveGrantAmount influence at ${claimData.tile.location}.")

            }, "Your $influenceOnTile influence on this tile isn't enough to grant $grantAmount. Do you want to spend influence " +
                    "to complete this?")
                    .sendOnCondition(influenceOnTile < grantAmount)

        }, "This player is not in this nation and won't be able to use the claim unless they switch allegiance! Are you sure?")
                .sendOnCondition(targetPlayer.allegiance != context.guild.nation)

    }

    override fun info(authorityLevel: AuthorityLevel?, isPublic: Boolean): String? {
        return "**.grant LOCATION AMOUNT PLAYER** - grants some portion of your land to a player."
    }

}