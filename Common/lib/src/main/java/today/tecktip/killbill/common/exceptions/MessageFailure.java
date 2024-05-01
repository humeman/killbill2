package today.tecktip.killbill.common.exceptions;

/**
 * Denotes failures in messaging either the API or gameserver.
 * @author cs
 */
public class MessageFailure extends Exception {
    /**
     * Constructs a message failure exception with a reason.
     * @param reason Reason for the exception
     */
    public MessageFailure(final String reason) {
        super(reason);
    }

    /**
     * Constructs a message failure exception with a reason and cause.
     * @param reason Reason for the exception
     * @param cause Causing exception to wrap
     */
    public MessageFailure(final String reason, final Throwable cause) {
        super(reason, cause);
    }
}
