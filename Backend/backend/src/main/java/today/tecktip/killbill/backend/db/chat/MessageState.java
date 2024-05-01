package today.tecktip.killbill.backend.db.chat;

/**
 * Stores if a message has been seen by the user or not
 * @author cz
 */
public enum MessageState {
    /**
     * Has been seen
     */
    READ,
    /**
     * Has not been seen
     */
    UNREAD
}