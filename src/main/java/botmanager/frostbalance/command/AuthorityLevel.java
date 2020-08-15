package botmanager.frostbalance.command;

public enum AuthorityLevel {

    /**
     * Possessed by all users.
     */
    GENERIC(0),

    /**
     * Possessed by users with administrator privileges in the guild
     */
    SERVER_ADMIN(1),

    /**
     * Possessed by users with the leader rank in the guild
     */
    SERVER_LEADER(2),

    /**
     * Possessed by users with the Frostbalance rank in the guild - in some
     * contexts this rank doesn't make sense
     */
    GUILD_ADMIN(3),

    /**
     * Possessed by the user who owns the guild - in some contexts this rank
     * doesn't make sense
     */
    GUILD_OWNER(4),

    /**
     * Possessed by manually whitelisted Discord user ids.
     */
    BOT_ADMIN(5),


    /**
     * Possessed by the Bot itself and by Shade. There is no higher level of authority.
     */
    BOT(6);

    int rank;

    AuthorityLevel(int rank) {
        this.rank = rank;
    }

    public boolean hasAuthority(AuthorityLevel level) {
        return this.rank >= level.rank;
    }
}
