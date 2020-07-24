package botmanager.frostbalance;

import botmanager.frostbalance.generic.AuthorityLevel;

public enum OptionFlag {

    MAIN("Main Server Network", "\uD83C\uDF10", AuthorityLevel.BOT_ADMIN),

    RED("Red Color Scheme", "\uD83D\uDFE5", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    GREEN("Green Color Scheme", "\uD83D\uDFE9", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    BLUE("Blue Color Scheme", "\uD83D\uDFE6", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),

    TUTORIAL("Tutorial Server", "\uD83D\uDCDA", AuthorityLevel.GUILD_ADMIN),
    TEST("Experimental Content", "⚠️", AuthorityLevel.GUILD_ADMIN);

    String label;
    String emoji;
    AuthorityLevel authorityToChange;
    ExclusivityGroup exclusivityGroup;

    OptionFlag(String label, String emoji, AuthorityLevel authorityToChange, ExclusivityGroup exclusivityGroup) {
        this.label = label;
        this.emoji = emoji;
        this.authorityToChange = authorityToChange;
        this.exclusivityGroup = exclusivityGroup;
    }

    OptionFlag(String label, String emoji, AuthorityLevel authorityToChange) {
        this(label, emoji, authorityToChange, null);
    }

    public String getLabel() {
        return label;
    }

    public String getEmoji() {
        return emoji;
    }

    public AuthorityLevel getAuthorityToChange() {
        return authorityToChange;
    }

    public boolean isExclusiveWith(OptionFlag toggledFlag) {
        return (exclusivityGroup != null && toggledFlag.exclusivityGroup.equals(exclusivityGroup));
    }

    private enum ExclusivityGroup {
        COLOR;
    }
}
