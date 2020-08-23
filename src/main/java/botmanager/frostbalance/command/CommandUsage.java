package botmanager.frostbalance.command;

import botmanager.frostbalance.Frostbalance;

public class CommandUsage {

    FrostbalanceCommand command;

    AuthorityLevel requiredAuthority; //required authority to run the command in this way
    ContextLevel requiredContext; //required context to run the command in this way

    String syntax;
    String effect;

    public String toString() {
        return "**" + Frostbalance.bot.getPrefix() + command.getMainAlias() + " " + syntax + "** - " + effect;
    }

}
