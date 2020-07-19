package botmanager.frostbalance;

import botmanager.frostbalance.generic.AuthorityLevel;

public enum OptionFlag {

    MAIN("Main Server Network", AuthorityLevel.BOT_ADMIN),
    TUTORIAL("Tutorial Server", AuthorityLevel.GUILD_ADMIN),
    TEST("Experimental Content", AuthorityLevel.GUILD_ADMIN);

    String label;
    AuthorityLevel authorityToChange;

    OptionFlag(String label, AuthorityLevel authorityToChange) {
        this.label = label.toUpperCase();
        this.authorityToChange = authorityToChange;
    }
}
