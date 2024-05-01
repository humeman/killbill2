package today.tecktip.killbill.backend.exceptions;

/**
 * Basic SQL not found error. <strong>This does not return a 404. Use InvalidArgumentException.</strong>
 * @author cs
 */
public class NotFoundException extends RuntimeException {
    /**
     * Constructs a new Not Found exception.
     * <p>
     * <strong>This does not return a 404. Use InvalidArgumentException.</strong>
     * @param message Reason for exception
     */
    public NotFoundException(String message) {
        super(message);
    }
}