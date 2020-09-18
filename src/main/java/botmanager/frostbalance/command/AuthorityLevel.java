package botmanager.frostbalance.command;

public enum AuthorityLevel {

    /**
     * Possessed by all users.
     */
    GENERIC(0),

    /**
     * Possessed by users with the 'kick' privilege in the guild
     */
    NATION_SECURITY(1),

    /**
     * Possessed by users with the leader rank in the guild
     */
    NATION_LEADER(2),

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
     * Possessed by users granted special permission over a map, which is
     * a network of guilds
     */
    MAP_ADMIN(5),

    /**
     * Possessed by the player who has requested access to a map
     */
    MAP_OWNER(6),

    /**
     * Possessed by manually whitelisted Discord user ids.
     */
    BOT_ADMIN(7),

    /**
     * Possessed by the Bot itself. There is no higher level of authority.
     */
    SELF(8);

    int rank;

    AuthorityLevel(int rank) {
        this.rank = rank;
    }

    public boolean hasAuthority(AuthorityLevel level) {
        return this.rank >= level.rank;
    }
}
