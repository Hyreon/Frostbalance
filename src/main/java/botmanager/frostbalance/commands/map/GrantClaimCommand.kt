package botmanager.frostbalance.commands.map

import botmanager.frostbalance.Frostbalance
import botmanager.frostbalance.Influence
import botmanager.frostbalance.command.*
import botmanager.frostbalance.menu.ConfirmationMenu

class GrantClaimCommand(bot: Frostbalance) : FrostbalanceGuildCommand(bot, arrayOf("grant"), AuthorityLevel.GENERIC, ContextLevel.ANY) {

    override fun executeWithGuild(context: GuildMessageContext, vararg params: String) {

        val argumentStream = ArgumentStream(params)
        val claimData = argumentStream.nextCoordinate()?.let { context.gameNetwork.worldMap.getTile(it).claimData }
                ?: return context.sendResponse("Could not recognize location '${argumentStream.lastArgument}'.")
        val grantAmount = argumentStream.nextInfluence()
                ?: return context.sendResponse("The amount '${argumentStream.lastArgument}' wasn't recognized as a valid influence amount.")
        val targetPlayer = bot.getUserByName(argumentStream.exhaust())?.playerIn(context.gameNetwork)
                ?: return context.sendResponse("Could not find player '${argumentStream.lastArgument}'.")

        ConfirmationMenu(bot, context, {

            val influenceOnTile = claimData.getClaim(context.player, context.guild.nation)?.strength
                    ?: Influence.none()

            ConfirmationMenu(bot, context, {

                if (influenceOnTile < grantAmount) {
                    claimData.addClaim(context.player, grantAmount.subtract(influenceOnTile))
                }

                claimData.transferToClaim(context.member, targetPlayer, grantAmount)
                context.sendResponse("You have given ${targetPlayer.name} $grantAmount influence at ${claimData.tile.location}.")

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