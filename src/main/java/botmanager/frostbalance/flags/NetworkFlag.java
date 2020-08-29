package botmanager.frostbalance.flags;

import botmanager.frostbalance.command.AuthorityLevel;

public enum NetworkFlag {

    TUTORIAL("Tutorial Server", "\uD83D\uDCDA", AuthorityLevel.BOT_ADMIN, ExclusivityGroup.GAME_TYPE),
    EXPERIMENTAL("Experimental Content", "⚠️", AuthorityLevel.GUILD_ADMIN);

    String label;
    String emoji;
    AuthorityLevel authorityToChange;
    ExclusivityGroup exclusivityGroup;

    NetworkFlag(String label, String emoji, AuthorityLevel authorityToChange, ExclusivityGroup exclusivityGroup) {
        this.label = label;
        this.emoji = emoji;
        this.authorityToChange = authorityToChange;
        this.exclusivityGroup = exclusivityGroup;
    }

    NetworkFlag(String label, String emoji, AuthorityLevel authorityToChange) {
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

    public boolean isExclusiveWith(NetworkFlag toggledFlag) {
        return (exclusivityGroup != null && exclusivityGroup.equals(toggledFlag.exclusivityGroup));
    }

    private enum ExclusivityGroup {
        COLOR, GAME_TYPE;
    }

}
