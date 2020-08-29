package botmanager.frostbalance.flags;

import botmanager.frostbalance.command.AuthorityLevel;

public enum OldOptionFlag {

    MAIN("Main Server Network", "\uD83C\uDF10", AuthorityLevel.BOT_ADMIN, ExclusivityGroup.GAME_TYPE),
    TUTORIAL("Tutorial Server", "\uD83D\uDCDA", AuthorityLevel.BOT_ADMIN, ExclusivityGroup.GAME_TYPE),

    RED("Red Color Scheme", "\uD83D\uDFE5", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    GREEN("Green Color Scheme", "\uD83D\uDFE9", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),
    BLUE("Blue Color Scheme", "\uD83D\uDFE6", AuthorityLevel.GUILD_ADMIN, ExclusivityGroup.COLOR),

    TEST("Experimental Content", "⚠️", AuthorityLevel.GUILD_ADMIN);

    String label;
    String emoji;
    AuthorityLevel authorityToChange;
    ExclusivityGroup exclusivityGroup;

    OldOptionFlag(String label, String emoji, AuthorityLevel authorityToChange, ExclusivityGroup exclusivityGroup) {
        this.label = label;
        this.emoji = emoji;
        this.authorityToChange = authorityToChange;
        this.exclusivityGroup = exclusivityGroup;
    }

    OldOptionFlag(String label, String emoji, AuthorityLevel authorityToChange) {
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

    public boolean isExclusiveWith(OldOptionFlag toggledFlag) {
        return (exclusivityGroup != null && exclusivityGroup.equals(toggledFlag.exclusivityGroup));
    }

    private enum ExclusivityGroup {
        COLOR, GAME_TYPE;
    }
}
