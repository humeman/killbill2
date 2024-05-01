package today.tecktip.killbill.frontend.exceptions;

/**
 * Notes that something catastrophic has happened. Will quite possibly result in a game crash.
 * <p>
 * Good for impossible states, data that isn't as expected, etc.
 * @author cs
 */
public class CatastrophicException extends RuntimeException {
    /**
     * Constructs a new Catastrophic Exception with a message. If this exception is the result of another exception,
     * please use {@link #CatastrophicException(String, Throwable)} instead.
     * @param message Reason
     */
    public CatastrophicException(final String message) {
        super(message);
    }

    /**
     * Constructs a new Catastrophic Exception with a message and cause.
     * @param message Reason
     * @param cause Exception which caused this
     */
    public CatastrophicException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
