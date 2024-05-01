package today.tecktip.killbill.backend.db.friends;

/**
 * Enum representing the possible states a friend link can be in.
 * @author cs
 */
public enum FriendState {
    /**
     * 'To' has invited 'From' to be a friend.
     */
    INVITED,
    /**
     * Both of the involved users are friends.
     */
    FRIENDS,
    /**
     * 'To' has blocked 'From' from their life.
     */
    BLOCKED
}
