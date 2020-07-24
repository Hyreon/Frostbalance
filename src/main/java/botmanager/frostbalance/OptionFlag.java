package botmanager.frostbalance;

import botmanager.frostbalance.generic.AuthorityLevel;

public enum OptionFlag {

    RED("Red Color Scheme", "\uD83D\uDFE5", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    GREEN("Green Color Scheme", "\uD83D\uDFE9", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    BLUE("Blue Color Scheme", "\uD83D\uDFE6", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    MAIN("Main Server Network", "\uD83C\uDF10", AuthorityLevel.BOT_ADMIN),
    TUTORIAL("Tutorial Server", "\uD83D\uDCDA", AuthorityLevel.GUILD_ADMIN),
    TEST("Experimental Content", "⚠️", AuthorityLevel.GUILD_ADMIN);

    String label;
    String emoji;
    AuthorityLevel authorityToChange;
    ExclusivityGroup exclusivityGroup;

    OptionFlag(String label, String emoji, AuthorityLevel authorityToChange, ExclusivityGroup exclusivityGroup) {
        this.label = label.toUpperCase();
        this.emoji = emoji;
        this.authorityToChange = authorityToChange;
        this.exclusivityGroup = exclusivityGroup;
    }

    OptionFlag(String label, String emoji, AuthorityLevel authorityToChange) {
        this(label, emoji, authorityToChange, null);
    }

    private enum ExclusivityGroup {
        COLOR;
    }
}
